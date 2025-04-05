package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class Settings : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oldPasswordField: TextInputEditText
    private lateinit var newPasswordField: TextInputEditText
    private lateinit var updatePasswordBtn: Button
    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Bind Views
        oldPasswordField = findViewById(R.id.oldPassword)
        newPasswordField = findViewById(R.id.newPassword)
        updatePasswordBtn = findViewById(R.id.confirmPasswordBtn)
        logoutBtn = findViewById(R.id.logoutBtn)

        // Update Password
        updatePasswordBtn.setOnClickListener {
            val oldPass = oldPasswordField.text.toString().trim()
            val newPass = newPasswordField.text.toString().trim()

            if (oldPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please enter both fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val email = user?.email

            if (user != null && email != null) {
                val credential = EmailAuthProvider.getCredential(email, oldPass)

                user.reauthenticate(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                oldPasswordField.text?.clear()
                                newPasswordField.text?.clear()
                            } else {
                                Toast.makeText(this, "Failed to update password: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Old password is incorrect.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout
        logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
            // Go back to login screen or main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}