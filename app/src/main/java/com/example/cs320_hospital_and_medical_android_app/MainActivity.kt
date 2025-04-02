package com.example.cs320_hospital_and_medical_android_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.BarcodeView


class MainActivity : AppCompatActivity() {

    // Firebase Initialization - And other things
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var debugQRReader = false
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String> // this is the permission launcher

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "You are now in Login Page")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PatientInformationActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Authentication failed!", Toast.LENGTH_LONG).show()
                }
            }
        }

        val forgotPW: TextView = findViewById(R.id.forgotPassword)
        forgotPW.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        val registerBtn: TextView = findViewById(R.id.registerBtn)
        registerBtn.setOnClickListener {
            val intent = Intent(this, AccountRegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkCameraPermission(): Boolean {
        Log.d("DEBUG", "Pause everything, ask camera permission first.")
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    // ✅ Just initializes the permission launcher, but doesn't ask yet
    private fun initPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("DEBUG", "Camera permission granted ✅")

                if (debugQRReader) {
                    startQRScannerFunction()
                }
            } else {
                Log.e("DEBUG", "Camera permission denied ❌")
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startQRScannerFunction() {
        Log.d("DEBUG", "Camera Permission finished, unpause everything.")

        val qrCodeScanner: BarcodeView = findViewById(R.id.qrBarcodeScanner)
        val editTextPid: EditText = findViewById(R.id.editTextPid)

        val qrScanner = QRCodeScannerClass(this, qrCodeScanner, editTextPid)
        qrScanner.startScanner()
        Log.d("DEBUG", "⌚ Scanner starts Scanning")

        val qrFrameContainer : FrameLayout = findViewById(R.id.qrFrameContainer)
        qrFrameContainer.setOnClickListener {
            Log.d("DEBUG","qrFrameContainer is clicked")
            if (!qrScanner.getIsScanning()) {
                qrScanner.startScanner()
            }
        }

        val btnToggleFlash : ImageButton = findViewById(R.id.btnToggleFlash)
        btnToggleFlash.setOnClickListener {
            qrScanner.toggleFlash()
        }
        val btnToggleAutoFocus : ImageButton = findViewById(R.id.btnToggleAutoFocus)
        btnToggleAutoFocus.setOnClickListener {
            qrScanner.toggleAutoFocus()
        }
    }
}
