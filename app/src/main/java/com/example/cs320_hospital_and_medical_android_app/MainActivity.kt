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
    private val dbHandler = DBHandlerClass()
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
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInBtn.isEnabled = false
            Log.d("DEBUG", "SignBTN enabled = false")

            dbHandler.authenticateUser(email, password) { mapInfo ->
                runOnUiThread {
                    signInBtn.isEnabled = true
                    Log.d("DEBUG", "SignBTN enabled = true")
                }

                if (mapInfo == null) {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                    return@authenticateUser
                }

                val role = mapInfo["role"] ?: return@authenticateUser
                val uid = mapInfo["uid"]
                val name = mapInfo["name"]

                if (mapInfo["status"] == "patient-notreg") {
                    patientRegistration(role)
                } else if (uid != null && name != null) {
                    directToDashboard(role, uid, name)
                } else {
                    Toast.makeText(this, "Invalid user profile data.", Toast.LENGTH_LONG).show()
                }
            }
        }

        initializeOtherAccess()
        autoLogin()
    }

    private fun patientRegistration(ROLE: String) {
        val intent = Intent(this, PatientInformation::class.java)
        intent.putExtra("ROLE", ROLE)
        startActivity(intent)
        finish()
    }

    private fun directToDashboard(ROLE: String, UID: String, NAME: String) {
        val intent = Intent(this, Dashboard::class.java)
        intent.putExtra("ROLE", ROLE)
        intent.putExtra("UID", UID)
        intent.putExtra("NAME", NAME)
        startActivity(intent)
        finish()
    }

    private fun initializeOtherAccess() {
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

    private fun autoLogin() {
        val user = auth.currentUser
        if (user != null) {
            Log.d("DEBUG", "Found previous login data")
            val firebaseUid = user.uid

            db.collection("users").document(firebaseUid).get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        val ROLE = userDoc.getString("role").toString()
                        val role = userDoc.getString("role")?.replaceFirstChar { it.uppercaseChar() }
                        val accountId = userDoc.getString("accountId")

                        if (role != null && accountId != null) {
                            db.collection("${role}s").document(accountId).get()
                                .addOnSuccessListener { profileDoc ->
                                    val firstName = profileDoc.getString("firstName") ?: ""
                                    val lastName = profileDoc.getString("lastName") ?: ""
                                    val name = "$firstName $lastName".trim()

                                    Log.e("NAME", "$firebaseUid")

                                    directToDashboard(ROLE, accountId, name)
                                }
                        }
                    }
                }
        }
    }
}

