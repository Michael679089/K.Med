package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

class AccountRegistration : AppCompatActivity() {
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText

    private lateinit var signUpBtn: Button
    private lateinit var loginBtn: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_registration)

        db = FirebaseFirestore.getInstance()

        // Account Info Fields
        emailField = findViewById(R.id.emailInput)
        passwordField = findViewById(R.id.passwordInput)

        // Buttons
        signUpBtn = findViewById(R.id.signupBtn)
        loginBtn = findViewById(R.id.loginBtn)

        auth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()        }

        signUpBtn.setOnClickListener {
            registerUser()
        }
    }

    data class AccountUser (
        val accountId: String = "",
        val role: String = "",
    )

    private fun registerUser() {

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        if (!validateInputs(email, password)) return

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val accountuser = AccountUser(
                    accountId = "",
                    role = "patient-notreg"
                )

                auth.uid?.let {
                    db.collection("users")
                        .document(it)
                        .set(accountuser)
                }

                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                handleRegistrationError(task.exception)
            }
        }
    }

    private fun validateInputs(
        email: String, password: String
    ): Boolean {
        return when {

            email.isEmpty() || password.isEmpty() -> {
                showToast("Please enter both email and password.")
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Please enter a valid email address.")
                return false
            }

            password.length < 6 -> {
                showToast("Password must be at least 6 characters long.")
                return false
            }

                // Check for at least one lowercase letter
            !password.any { it.isLowerCase() } -> {
                showToast("Password must contain at least one lowercase letter.")
                return false
            }

            // Check for at least one uppercase letter
            !password.any { it.isUpperCase() } -> {
                showToast("Password must contain at least one uppercase letter.")
                return false
            }

            // Check for at least one digit
            !password.any { it.isDigit() } -> {
                showToast("Password must contain at least one number.")
                return false
            }

            // Check for at least one special character
            !password.any { it in "!@#$%^&*()_+[]{}|;:,.<>?/" } -> {
                showToast("Password must contain at least one special character.")
                return false
            }

            else -> true
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleRegistrationError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, "This email is already registered", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(
                    this,
                    "Registration failed: ${exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}