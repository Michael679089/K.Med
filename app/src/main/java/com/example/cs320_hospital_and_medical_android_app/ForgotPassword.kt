package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailVerify: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.forgot_password)

        auth = FirebaseAuth.getInstance()

        // Initialize UI components
        emailVerify = findViewById(R.id.emailVerify)
        val emailPassReset: Button = findViewById(R.id.emailPassReset)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val emailInputState: TextView = findViewById(R.id.emailInputState)


        // Handle user verification button click
        emailPassReset.setOnClickListener {
            val email = emailInput.text.toString()

            if (email.isEmpty()) {
                emailInputState.text = "Please input a valid email"
                emailInputState.setTextColor(Color.parseColor("#FF0000"))
            } else {
                // Send password reset email
                auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Email is valid and exists
                        emailInputState.text = null
                        Toast.makeText(
                            this,
                            "Password reset email sent successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        emailPassReset.text = "Resend Email"
                    } else {
                        val exception = task.exception
                        emailInputState.text = "An error occurred. Please try again."
                        emailInputState.setTextColor(Color.parseColor("#FF0000"))
                    }
                }
            }
        }
    }
}
