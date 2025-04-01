package com.example.cs320_hospital_and_medical_android_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
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
        emailInput.setText("james.billate@ciit.edu.ph") //For Testing Only

        val passwordInput: EditText = findViewById(R.id.passwordInput)
        passwordInput.setText("123456789") //For Testing Only

        val signInBtn: Button = findViewById(R.id.signinBtn)

        signInBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInBtn.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    val firebaseUid = user?.uid

                    db.collection("users")
                        .whereEqualTo("firebaseUid", firebaseUid)
                        .get()
                        .addOnSuccessListener { documents ->
                            signInBtn.isEnabled = true
                            if (!documents.isEmpty) {
                                val userDoc = documents.documents[0]
                                val role = userDoc.getString("role")
                                val customUid = userDoc.getString("uid")
                                val name = userDoc.getString("name")

                                if (role != null && customUid != null && name != null) {
                                    Toast.makeText(this, "Welcome, $role!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, Dashboard::class.java)
                                    intent.putExtra("role", role)
                                    intent.putExtra("uid", customUid)
                                    intent.putExtra("name", name)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Invalid user data.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(this, "User profile not found.", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener {
                            signInBtn.isEnabled = true
                            Log.e("FIRESTORE", "Error fetching user profile", it)
                            Toast.makeText(this, "Something went wrong: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    signInBtn.isEnabled = true
                    Toast.makeText(this, "Authentication failed: ${it.message}", Toast.LENGTH_LONG).show()
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

        // Auto-login for logged-in users (if not logged out)
        val user = auth.currentUser
        if (user != null) {
            val firebaseUid = user.uid

            db.collection("users")
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val userDoc = documents.documents[0]
                        val role = userDoc.getString("role")
                        val customUid = userDoc.getString("uid")
                        val name = userDoc.getString("name")

                        val intent = Intent(this, Dashboard::class.java)
                        intent.putExtra("role", role)
                        intent.putExtra("uid", customUid)
                        intent.putExtra("name", name)
                        startActivity(intent)
                        finish()
                    }
                }
        }
    }

}

