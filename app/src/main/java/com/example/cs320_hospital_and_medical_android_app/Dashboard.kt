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

            loadUserInfo()
            loadRoleButtons("doctor")
            loadScheduleCard("doctor")
    }

    private fun loadUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        val nameView = findViewById<TextView>(R.id.accountName)
        val idView = findViewById<TextView>(R.id.accountID)

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                nameView.text = doc.getString("name") ?: "No Name"
                idView.text = doc.getString("patientId") ?: "PID$userId"
            }
            .addOnFailureListener {
                nameView.text = "Unknown"
                idView.text = "PID$userId"
            }
    }


    // STEP 2: Load buttons layout depending on role
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

            if (role == "patient"){
                patientAccessPoint()
            } else if (role == "doctor"){
                doctorAccessPoint()
            }
        }

    }

    private fun patientAccessPoint() {

        val doctorBtn: LinearLayout = findViewById(R.id.doctorBtn)
        val scheduleBtn: LinearLayout = findViewById(R.id.scheduleBtn)
        val prescriptionBtn: LinearLayout = findViewById(R.id.prescriptionBtn)
        val editPatientBtn: ImageView = findViewById(R.id.editPatientBtn)

        doctorBtn.setOnClickListener(){
            val intent = Intent(this, DoctorSchedule::class.java)
            startActivity(intent)
        }

        scheduleBtn.setOnClickListener(){
            val intent = Intent(this, PatientAppointment::class.java)
            startActivity(intent)
        }

        prescriptionBtn.setOnClickListener(){
            val intent = Intent(this, Prescription::class.java)
            startActivity(intent)
        }

        editPatientBtn.setOnClickListener(){
            val intent = Intent(this, PatientInformation::class.java)
            startActivity(intent)
        }
    }

    private fun doctorAccessPoint() {

        val doctorAccessSchedule: LinearLayout = findViewById(R.id.doctorAccessSchedule)

        doctorAccessSchedule.setOnClickListener(){
            val intent = Intent(this, DoctorSchedule::class.java)
            startActivity(intent)
        }

    }

    // STEP 3: Load schedule card section based on role
    private fun loadScheduleCard(role: String) {
        val scheduleContainer = findViewById<FrameLayout>(R.id.scheduleCardContent)
        val inflater = LayoutInflater.from(this)

        // Default scenario — simulate patient booked (change logic later based on data)
        val layoutRes = when (role) {
            "patient" -> R.layout.dashboard_schedule_patient_booked
            // Add more here for different roles (doctor/nurse) later
            else -> null
        }

        layoutRes?.let {
            val view = inflater.inflate(it, scheduleContainer, false)

            // Placeholder content for now
            val appointmentDate = "March 5, 2025"
            val doctorName = "Dr. Fillman\nCosman"

            // Only try to update if layout contains these views
            val dateText = view.findViewById<TextView?>(R.id.textAppointmentDate)
            val doctorText = view.findViewById<TextView?>(R.id.textDoctorName)

            dateText?.text = appointmentDate
            doctorText?.text = doctorName

            scheduleContainer.removeAllViews()
            scheduleContainer.addView(view)
        }
    }

    // STEP 4: Get user role (for now return static/dummy until Firebase is configured)


}

/**
 * private fun loadScheduleCard(role: String) {
 *     val scheduleContainer = findViewById<FrameLayout>(R.id.scheduleCardContent)
 *     val inflater = LayoutInflater.from(this)
 *
 *     val currentUser = FirebaseAuth.getInstance().currentUser
 *     val userId = currentUser?.uid ?: return
 *     val db = FirebaseFirestore.getInstance()
 *
 *     when (role) {
 *         "patient" -> {
 *             db.collection("appointments").document(userId).get()
 *                 .addOnSuccessListener { doc ->
 *                     val status = doc.getString("status") // "booked", "queue", "none"
 *                     val layoutRes = when (status) {
 *                         "booked" -> R.layout.dashboard_schedule_patient_booked
 *                         "queue" -> R.layout.dashboard_schedule_patient_queue
 *                         else -> R.layout.dashboard_schedule_none
 *                     }
 *
 *                     val view = inflater.inflate(layoutRes, scheduleContainer, false)
 *
 *                     view.findViewById<TextView?>(R.id.textAppointmentDate)?.text =
 *                         doc.getString("date") ?: "N/A"
 *
 *                     view.findViewById<TextView?>(R.id.textDoctorName)?.text =
 *                         doc.getString("doctor") ?: "TBA"
 *
 *                     view.findViewById<TextView?>(R.id.textQueueNumber)?.text =
 *                         doc.get("queue")?.toString() ?: "0"
 *
 *                     view.findViewById<TextView?>(R.id.textQueueDestination)?.text =
 *                         doc.getString("destination") ?: "Room"
 *
 *                     scheduleContainer.removeAllViews()
 *                     scheduleContainer.addView(view)
 *                 }
 *         }
 *
 *         "doctor" -> {
 *             db.collection("schedules").document(userId).get()
 *                 .addOnSuccessListener { doc ->
 *                     val hasSchedule = doc.getBoolean("hasSchedule") ?: false
 *                     val layoutRes = if (hasSchedule)
 *                         R.layout.dashboard_schedule_doctor
 *                     else
 *                         R.layout.dashboard_schedule_none
 *
 *                     val view = inflater.inflate(layoutRes, scheduleContainer, false)
 *                     scheduleContainer.removeAllViews()
 *                     scheduleContainer.addView(view)
 *                 }
 *         }
 *
 *         "nurse" -> {
 *             db.collection("assignments").document(userId).get()
 *                 .addOnSuccessListener { doc ->
 *                     val hasTask = doc.getBoolean("hasTask") ?: false
 *                     val layoutRes = if (hasTask)
 *                         R.layout.dashboard_schedule_nurse
 *                     else
 *                         R.layout.dashboard_schedule_none
 *
 *                     val view = inflater.inflate(layoutRes, scheduleContainer, false)
 *                     scheduleContainer.removeAllViews()
 *                     scheduleContainer.addView(view)
 *                 }
 *         }
 *
 *         else -> {
 *             val view = inflater.inflate(R.layout.dashboard_schedule_none, scheduleContainer, false)
 *             scheduleContainer.removeAllViews()
 *             scheduleContainer.addView(view)
 *         }
 *     }
 * }
 *
 * */