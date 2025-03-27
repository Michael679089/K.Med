package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    //Firebase Initialization
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        //Firebase Initialization
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val signInBtn: Button = findViewById(R.id.signinBtn)

        signInBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    val user = auth.currentUser
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, PatientInformation::class.java)
                    startActivity(intent)

                } else {
                    // If sign-in fails
                    Toast.makeText(this, "Authentication failed!", Toast.LENGTH_LONG).show()
                }
            }
        }

        val forgotPW: TextView = findViewById(R.id.forgotPassword)

        forgotPW.setOnClickListener(){
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        val registerBtn: TextView = findViewById(R.id.registerBtn)

        registerBtn.setOnClickListener(){
            val intent = Intent(this, AccountRegistration::class.java)
            startActivity(intent)
        }


    }
}

