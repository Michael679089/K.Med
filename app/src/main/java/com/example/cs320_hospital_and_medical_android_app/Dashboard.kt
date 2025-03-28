package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        val userRole = getUserRole()

        loadRoleButtons(userRole)
        loadScheduleCard(userRole)
    }

    // STEP 1: Load buttons layout depending on role
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
            val view = inflater.inflate(it, container, false)
            container.removeAllViews()
            container.addView(view)
        }
    }

    // STEP 2: Load schedule card section based on role
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

    // STEP 3: Get user role (for now return static/dummy until Firebase is configured)
    private fun getUserRole(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        // Later: fetch actual role from Firestore using userId
        return "patient" // Default for now
    }
}
