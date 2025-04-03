package com.example.cs320_hospital_and_medical_android_app

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.BarcodeView
// imports Related to camera permission
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.Manifest


class QRReader : AppCompatActivity() {

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String> // this is the permission launcher
    private lateinit var qrScanner: QRCodeScannerClass

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "You are now in QR Reader View")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.qr_reader)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize permission launcher
        initPermissionLauncher()

        Log.d("DEBUG", "Login Page > You are now looking at qr_reader.xml activity")
        setContentView(R.layout.qr_reader)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // I want to pause everything during the cameraPermission.
        if (checkCameraPermission()) {
            // Check Permission is asynchronous, this code runs only after user input camera permission.
            Log.d("DEBUG", "Camera permission already granted ✅")
            startQRScannerFunction()
        } else {
            requestPermission()
        }

        val qrFrameContainer: FrameLayout = findViewById(R.id.qrFrameContainer)
        qrFrameContainer.setOnClickListener {
            Log.d("DEBUG", "QRFrameScanner clicked")
            if (checkCameraPermission()) {
                if (!qrScanner.getIsScanning()) {
                    qrScanner.startScanner()
                }
            } else {
                openAppSettings()
                requestPermission()
            }
        }

        Log.d("DEBUG", "Reached end")
    }

    // # QR READING FUNCTIONS

    private fun checkCameraPermission(): Boolean {
        Log.d("DEBUG", "Pause everything, ask camera permission first.")
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun initPermissionLauncher() { // ## initializes camera permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("DEBUG", "Camera permission granted ✅")
                startQRScannerFunction()
            } else {
                Log.e("DEBUG", "Camera permission denied ❌")
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Tap the QR Scanner to open permission settings!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startQRScannerFunction() { // ## The Function after permission.
        Log.d("DEBUG", "Camera Permission finished, unpause everything.")

        val qrCodeScanner: BarcodeView = findViewById(R.id.qrBarcodeScanner)
        val editTextPid: EditText = findViewById(R.id.editTextPid)

        qrScanner = QRCodeScannerClass(this, qrCodeScanner, editTextPid)
        qrScanner.startScanner()
        Log.d("DEBUG", "⌚ Scanner starts Scanning")

        val qrFrameContainer: FrameLayout = findViewById(R.id.qrFrameContainer)
        qrFrameContainer.setOnClickListener {
            Log.d("DEBUG", "QRFrameScanner clicked")
            if (checkCameraPermission()) {
                if (!qrScanner.getIsScanning()) {
                    qrScanner.startScanner()
                }
            } else {
                openAppSettings()
                requestPermission()
            }
        }

        val btnToggleFlash: ImageButton = findViewById(R.id.btnToggleFlash)
        btnToggleFlash.setOnClickListener {
            qrScanner.toggleFlash()
        }
        val btnToggleAutoFocus: ImageButton = findViewById(R.id.btnToggleAutoFocus)
        btnToggleAutoFocus.setOnClickListener {
            qrScanner.toggleAutoFocus()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    // Override onResume to check permission after the user returns from settings
    override fun onResume() {
        super.onResume()

        // Check if camera permission is granted when the user returns from settings
        if (checkCameraPermission()) {
            // If permission is granted, start the scanner
            startQRScannerFunction()
        }
    }
}
