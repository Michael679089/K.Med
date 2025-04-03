package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.BarcodeView

class QRReader : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.qr_reader)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val barcodeArea: FrameLayout = findViewById(R.id.barcodeArea)
        val qrGuideOverlay: BarcodeView = barcodeArea.findViewById(R.id.qrGuideOverlay)

        val editTextPid: EditText = findViewById(R.id.editTextPid)

        val qrContainer: LinearLayout = findViewById(R.id.qrContainer)
        val toggleContainer: LinearLayout = qrContainer.findViewById(R.id.toggleContainer)
        val btnToggleFlash: ImageButton = toggleContainer.findViewById(R.id.btnToggleFlash)
        val btnToggleAutoFocus: ImageButton = toggleContainer.findViewById(R.id.btnToggleAutoFocus)

        val qrCodeScanner = QRCodeScannerClass(this, qrGuideOverlay, editTextPid)
        qrCodeScanner.getIsScanning()
        qrCodeScanner.startScanner()

        btnToggleAutoFocus.setOnClickListener() {
            qrCodeScanner.toggleAutoFocus()
            Toast.makeText(this, "Hi", Toast.LENGTH_SHORT).show()
        }

        btnToggleFlash.setOnClickListener() {
            qrCodeScanner.toggleFlash()
            Toast.makeText(this, "Hi", Toast.LENGTH_SHORT).show()
        }
    }
}