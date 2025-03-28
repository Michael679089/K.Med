package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        val userRole = getUserRole() // Step 1: Get current user role
        loadRoleButtons(userRole)    // Step 2: Dynamically load buttons based on role
    }


     // Load buttons depending on role.
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

    private fun getUserRole(): String {
        // Logic of fetching user data in Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return "guest"
    }
}
