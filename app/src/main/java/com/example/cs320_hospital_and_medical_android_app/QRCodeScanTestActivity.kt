package com.example.cs320_hospital_and_medical_android_app

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeView
import android.Manifest
import android.util.Log

class QRCodeScanTestActivity : AppCompatActivity() {

    // Global Variables
    private lateinit var scanner: BarcodeView
    private lateinit var qrCodeScanner: QRCodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_qrcode_test)

        scanner = findViewById(R.id.scanner)

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            initializeScanner() // ✅ Initialize scanner if permission is already granted
        }

        val toggleScannerButton: Button = findViewById(R.id.toggleScannerButton)
        val toggleLightButton: Button = findViewById(R.id.toggleLightBTN)
        val toggleAutoFocusButton: Button = findViewById(R.id.toggleAutoFocusBTN)

        toggleScannerButton.setOnClickListener {
            if (qrCodeScanner.isScanning()) {
                qrCodeScanner.stopScanner()
                toggleScannerButton.text = "Start Scanner"
            } else {
                qrCodeScanner.startScanner()
                toggleScannerButton.text = "Stop Scanner"
            }
            Log.d("Debug", "scannerButton Clicked")
        }
        toggleLightButton.setOnClickListener {
            qrCodeScanner.toggleFlash()
        }
        toggleAutoFocusButton.setOnClickListener {
            qrCodeScanner.toggleAutoFocus()
        }
    }

    private fun initializeScanner() {
        qrCodeScanner = QRCodeScanner(this, scanner) // Initialize QR code scanner
        qrCodeScanner.startScanner()
    }

    override fun onPause() { // Override: Stops scanner when activity is not visible
        super.onPause()
        qrCodeScanner.stopScanner()
    }

    override fun onDestroy() { // Override: Releases scanner resources properly
        super.onDestroy()
        qrCodeScanner.releaseScanner()
    }

    // Override: ✅ Restart scanner if permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            initializeScanner() // ✅ Start scanner after permission is granted
        } else {
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}

