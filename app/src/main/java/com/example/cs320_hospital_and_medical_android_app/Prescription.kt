package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ViewFlipper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
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

    private lateinit var ROLE: String
    private lateinit var UID: String

    private lateinit var recyclerPrescription: RecyclerView
    private val prescriptions = mutableListOf<PrescriptionModel>()

    private var selectedPatientId: String? = null
    private var selectedPatientName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "You are in Prescription Activity View")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prescription)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ROLE = intent.getStringExtra("ROLE").toString()
        UID = intent.getStringExtra("UID").toString()
        selectedPatientId = intent.getStringExtra("patientId")
        selectedPatientName = intent.getStringExtra("patientName")

        viewFlipper = findViewById(R.id.viewFlipper)
        recyclerPrescription = findViewById(R.id.recyclerPrescription)
        recyclerPrescription.layoutManager = LinearLayoutManager(this)
        recyclerPrescription.adapter = PrescriptionAdapter(prescriptions) { selected ->
            viewPrescription(selected)
        }

        listPrescription()
    }

    private fun listPrescription() {
        viewFlipper.displayedChild = 0

        val addPrescriptionBtn: ImageButton = findViewById(R.id.addPrescriptionBtn)

        // Add Prescription Button: Only visible for doctors
        if (ROLE == "doctor") {
            addPrescriptionBtn.visibility = View.VISIBLE
            addPrescriptionBtn.setOnClickListener { prescriptionForm() }
        } else {
            addPrescriptionBtn.visibility = View.GONE
        }

        // Fetch prescriptions from Firestore based on user role
        val query = if (ROLE == "doctor") {
            // Doctor views prescriptions for the selected patient
            db.collection("prescriptions")
                .whereEqualTo("patientId", selectedPatientId)
                .orderBy("createdAt")
        } else {
            // Patient views their own prescriptions
            db.collection("prescriptions")
                .whereEqualTo("patientId", UID)
                .orderBy("createdAt")
        }

        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                createToast("Failed to load prescriptions: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                prescriptions.clear()
                for (document in snapshot) {
                    val item = document.toObject(PrescriptionModel::class.java)
                    prescriptions.add(item)
                }
                recyclerPrescription.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun viewPrescription(prescription: PrescriptionModel) {
        viewFlipper.displayedChild = 1

        findViewById<TextView>(R.id.prescriptionDate).text = prescription.date
        findViewById<TextView>(R.id.prescriptionDetails).text = prescription.details
        findViewById<TextView>(R.id.doctorID).text = prescription.doctorId
        findViewById<TextView>(R.id.doctorNameText).text = prescription.doctorName


        val btnEdit: ImageButton = findViewById(R.id.btnEditPrescription)
        val btnDelete: ImageButton = findViewById(R.id.btnDeletePrescription)

        if (ROLE == "doctor") {
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

    private fun prescriptionForm(existing: PrescriptionModel? = null) {
        viewFlipper.displayedChild = 2

        val publishBtn: Button = findViewById(R.id.publishBtn)
        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        val reasonInput: EditText = findViewById(R.id.reasonInput)
        val doctorName: TextView = findViewById(R.id.doctor)
        val dateText: TextView = findViewById(R.id.date)

        // If we are editing an existing prescription, populate fields
        if (existing != null) {
            reasonInput.setText(existing.details)
            doctorName.text = existing.doctorName
            dateText.text = existing.date
        } else {
            // Initialize for creating a new prescription
            reasonInput.setText("")

            // Fetch the doctor's name asynchronously from Firestore
            db.collection("Doctors").document(UID)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("doctorName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "Unknown"
                    doctorName.text = "$firstName $lastName"
                }

            // Set today's date
            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val today = formatter.format(Date())
            dateText.text = today
        }

        publishBtn.setOnClickListener {
            val newPrescription = PrescriptionModel(
                id = existing?.id ?: db.collection("prescriptions").document().id, // Generate new ID if no existing prescription
                doctorId = UID,
                doctorName = doctorName.text.toString(),
                patientId = existing?.patientId ?: selectedPatientId.orEmpty(), // Use selectedPatientId for new prescription
                date = dateText.text.toString(),
                details = reasonInput.text.toString(),
                createdAt = Timestamp.now()
            )

            // Save the prescription to Firestore
            db.collection("prescriptions").document(newPrescription.id).set(newPrescription)
                .addOnSuccessListener {
                    createToast("Prescription saved")
                    listPrescription() // Refresh list after saving
                }
                .addOnFailureListener {
                    createToast("Failed to save prescription")
                }
        }

        cancelBtn.setOnClickListener {
            listPrescription() // Go back to the prescription list
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
