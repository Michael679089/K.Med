package com.example.cs320_hospital_and_medical_android_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.BarcodeView

class QRCodeScanTestActivity : AppCompatActivity(), QRCodeScanner.QRCodeScanListener {

    private lateinit var scanner: BarcodeView
    private lateinit var qrCodeScanner: QRCodeScanner
    private lateinit var resultText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan_qrcode_test)

        scanner = findViewById(R.id.scanner)

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            initializeScanner()
        }

        val toggleScannerButton: Button = findViewById(R.id.toggleScannerButton)
        val toggleLightButton: Button = findViewById(R.id.toggleLightBTN)
        val toggleAutoFocusButton: Button = findViewById(R.id.toggleAutoFocusBTN)

        toggleScannerButton.setOnClickListener {
            if (qrCodeScanner.getIsScanning()) {
                qrCodeScanner.stopScanner()
                toggleScannerButton.text = "Start Scanner"
            } else {
                qrCodeScanner.startScanner()
                toggleScannerButton.text = "Stop Scanner"
            }
        }
        toggleLightButton.setOnClickListener {
            qrCodeScanner.toggleFlash()
        }
        toggleAutoFocusButton.setOnClickListener {
            qrCodeScanner.toggleAutoFocus()
        }
    }

    private fun initializeScanner() {
        qrCodeScanner = QRCodeScanner(this, scanner, this) // Pass listener to receive results
        qrCodeScanner.startScanner()
    }

    override fun onQRCodeScanned(result: String) {
        runOnUiThread {
            resultText.setText(result) // ✅ Update UI with scanned result
        }
    }

    override fun onPause() {
        super.onPause()
        qrCodeScanner.stopScanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        qrCodeScanner.releaseScanner()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            initializeScanner()
        } else {
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}
