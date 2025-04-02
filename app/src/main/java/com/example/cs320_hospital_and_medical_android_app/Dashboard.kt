package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout


class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)


        // Load User Info
        val role = intent.getStringExtra("role") ?: return
        val name = intent.getStringExtra("name") ?: "No Name"
        val customUid = intent.getStringExtra("uid") ?: "Unknown"

        val nameView = findViewById<TextView>(R.id.accountName)
        val idView = findViewById<TextView>(R.id.accountID)
        nameView.text = name
        idView.text = customUid

        loadRoleButtons(role)
        loadScheduleCard(role)

    }

    // Load RBA Buttons
    private fun loadRoleButtons(role: String) {
        val container = findViewById<FrameLayout>(R.id.buttonSectionContainer)
        val inflater = LayoutInflater.from(this)

        val layoutRes = when (role) {
            "patient" -> R.layout.dashboard_buttons_patient
            "doctor" -> R.layout.dashboard_buttons_doctor
            "nurse" -> R.layout.dashboard_buttons_nurse
            else -> null
        }

        layoutRes?.let {
            container.removeAllViews()
            val view = inflater.inflate(it, container, false)
            container.addView(view)

            // Now attach role-based listeners
            when (role) {
                "patient" -> setupPatientButtons(view)
                "doctor" -> setupDoctorButtons(view)
                "nurse"  -> setupNurseButtons(view)
            }
        }
    }

    // Button Listeners
    private fun setupPatientButtons(view: View) {
        val doctorBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.scheduleBtn)
        val prescriptionBtn = view.findViewById<LinearLayout>(R.id.prescriptionBtn)

        doctorBtn.setOnClickListener {
            startActivity(Intent(this, DoctorSchedule::class.java))
        }

        scheduleBtn.setOnClickListener {
            startActivity(Intent(this, PatientAppointment::class.java))
        }

        prescriptionBtn.setOnClickListener {
            startActivity(Intent(this, Prescription::class.java))
        }
    }

    private fun setupDoctorButtons(view: View) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.doctorAccessSchedule)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("role", "doctor")
            startActivity(intent)
        }

        scheduleBtn.setOnClickListener {
            startActivity(Intent(this, DoctorSchedule::class.java))
        }
    }

    private fun setupNurseButtons(view: View) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val doctorBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("role", "nurse")
            startActivity(intent)
        }

        doctorBtn.setOnClickListener {
            startActivity(Intent(this, DoctorSchedule::class.java))
        }
    }


    // Load Schedule Card Depending on scenario
    private fun loadScheduleCard(role: String) {
        val scheduleContainer = findViewById<FrameLayout>(R.id.scheduleCardContent)
        val inflater = LayoutInflater.from(this)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val setScheduleLayout: (Int, Map<Int, String?>) -> Unit = { layoutRes, textMap ->
            val view = inflater.inflate(layoutRes, scheduleContainer, false)
            for ((id, value) in textMap) {
                view.findViewById<TextView?>(id)?.text = value
            }
            scheduleContainer.removeAllViews()
            scheduleContainer.addView(view)
        }

        when (role) {
            "patient" -> {
                db.collection("appointments").document(userId).get()
                    .addOnSuccessListener { doc ->
                        val status = doc.getString("status")
                        val layoutRes = when (status) {
                            "booked" -> R.layout.dashboard_schedule_patient_booked
                            "queue" -> R.layout.dashboard_schedule_patient_queue
                            else -> R.layout.dashboard_schedule_none
                        }

                        val textMap = mapOf(
                            R.id.textAppointmentDate to doc.getString("date"),
                            R.id.textDoctorName to doc.getString("doctor"),
                            R.id.textQueueNumber to doc.get("queue")?.toString(),
                            R.id.textQueueDestination to doc.getString("destination")
                        )

                        setScheduleLayout(layoutRes, textMap)
                    }
            }

            "doctor" -> {
                db.collection("schedules").document(userId).get()
                    .addOnSuccessListener { doc ->
                        val layoutRes = if (doc.getBoolean("hasSchedule") == true)
                            R.layout.dashboard_schedule_doctor
                        else
                            R.layout.dashboard_schedule_none

                        setScheduleLayout(layoutRes, emptyMap())
                    }
            }

            "nurse" -> {
                db.collection("assignments").document(userId).get()
                    .addOnSuccessListener { doc ->
                        val layoutRes = if (doc.getBoolean("hasTask") == true)
                            R.layout.dashboard_schedule_nurse
                        else
                            R.layout.dashboard_schedule_none

                        setScheduleLayout(layoutRes, emptyMap())
                    }
            }

            else -> {
                setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
            }
        }
    }
}
