package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.cs320_hospital_and_medical_android_app.models.PrescriptionModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Prescription : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userRole: String

    private lateinit var recyclerPrescription: RecyclerView
    private val prescriptions = mutableListOf<PrescriptionModel>()

    // Patient info passed via Intent
    private var selectedPatientId: String? = null
    private var selectedPatientName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "You are in Prescription Activity View")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prescription)

        // Firebase Auth & Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Extract role & patient info from Intent
        userRole = intent.getStringExtra("ROLE") ?: "patient"
        selectedPatientId = intent.getStringExtra("patientId")
        selectedPatientName = intent.getStringExtra("patientName")

        Log.d("DEBUG", "Intent patientId: $selectedPatientId")
        Log.d("DEBUG", "Intent patientName: $selectedPatientName")

        viewFlipper = findViewById(R.id.viewFlipper)
        recyclerPrescription = findViewById(R.id.recyclerPrescription)

        // Show add button
        val addPrescriptionBtn: ImageButton = findViewById(R.id.addPrescriptionBtn)
        if (userRole == "doctor") {
            addPrescriptionBtn.visibility = View.VISIBLE
            addPrescriptionBtn.setOnClickListener { prescriptionForm() }
        } else {
            addPrescriptionBtn.visibility = View.GONE
        }

        listPrescription()
    }

    // Load prescriptions from Firestore
    private fun listPrescription() {
        viewFlipper.displayedChild = 0

        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("prescriptions")
            .whereEqualTo(if (userRole == "doctor") "doctorId" else "patientId", currentUserId)
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { result ->
                prescriptions.clear()
                for (document in result) {
                    val item = document.toObject(PrescriptionModel::class.java)
                    prescriptions.add(item)
                }

                recyclerPrescription.adapter = PrescriptionAdapter(prescriptions) { selected ->
                    viewPrescription(selected)
                }
            }
            .addOnFailureListener {
                createToast("Failed to load prescriptions")
            }
    }

    // View a selected prescription in detail view
    private fun viewPrescription(prescription: PrescriptionModel) {
        viewFlipper.displayedChild = 1

        findViewById<TextView>(R.id.prescriptionDate).text = prescription.date
        findViewById<TextView>(R.id.prescriptionDetails).text = prescription.details
        findViewById<TextView>(R.id.doctorName).text = prescription.doctorName
        findViewById<TextView>(R.id.doctorID).text = prescription.doctorId

        val btnEdit: ImageButton = findViewById(R.id.btnEditPrescription)
        val btnDelete: ImageButton = findViewById(R.id.btnDeletePrescription)

        if (userRole == "doctor") {
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            btnEdit.setOnClickListener { prescriptionForm(prescription) }

            btnDelete.setOnClickListener {
                db.collection("prescriptions").document(prescription.id).delete()
                    .addOnSuccessListener {
                        createToast("Prescription deleted")
                        listPrescription()
                    }
                    .addOnFailureListener {
                        createToast("Failed to delete")
                    }
            }
        } else {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }
    }

    // Create or edit a prescription
    private fun prescriptionForm(existing: PrescriptionModel? = null) {
        viewFlipper.displayedChild = 2

        val publishBtn: Button = findViewById(R.id.publishBtn)
        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        val reasonInput: EditText = findViewById(R.id.reasonInput)
        val doctorName: TextView = findViewById(R.id.doctor)
        val dateText: TextView = findViewById(R.id.date)

        val uid = auth.currentUser?.uid ?: return

        // Edit existing
        if (existing != null) {
            reasonInput.setText(existing.details)
            doctorName.text = existing.doctorName
            dateText.text = existing.date
        } else {
            // New entry
            reasonInput.setText("")

            val currentUser = auth.currentUser
            doctorName.text = currentUser?.displayName ?: "Dr. Unknown"

            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val today = formatter.format(Date())
            dateText.text = today
        }

        publishBtn.setOnClickListener {
            val newPrescription = PrescriptionModel(
                id = existing?.id ?: db.collection("prescriptions").document().id,
                doctorId = uid,
                doctorName = doctorName.text.toString(),
                patientId = existing?.patientId ?: selectedPatientId.orEmpty(), // ✅ FIXED: Uses passed patientId
                date = dateText.text.toString(),
                details = reasonInput.text.toString(),
                createdAt = Timestamp.now()
            )

            Log.d("DEBUG", "Saving prescription: $newPrescription")

            db.collection("prescriptions").document(newPrescription.id).set(newPrescription)
                .addOnSuccessListener {
                    createToast("Prescription saved")
                    listPrescription()
                }
                .addOnFailureListener {
                    createToast("Failed to save prescription")
                }
        }

        cancelBtn.setOnClickListener {
            listPrescription()
        }
    }

    private fun createToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (viewFlipper.displayedChild > 0) {
            listPrescription()
        } else {
            super.onBackPressed()
        }
    }

    inner class PrescriptionAdapter(
        private val items: List<PrescriptionModel>,
        private val onClick: (PrescriptionModel) -> Unit
    ) : RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder>() {

        inner class PrescriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: PrescriptionModel) {
                val date = itemView.findViewById<TextView>(R.id.datePrescribed)
                val name = itemView.findViewById<TextView>(R.id.doctorName)
                val viewBtn = itemView.findViewById<ImageView>(R.id.viewPrescriptionBtn)

                date.text = item.date
                name.text = item.doctorName

                viewBtn.setOnClickListener {
                    onClick(item)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
            val view = layoutInflater.inflate(R.layout.prescription_card, parent, false)
            return PrescriptionViewHolder(view)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
            holder.bind(items[position])
        }
    }
}
