package com.example.cs320_hospital_and_medical_android_app

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.journeyapps.barcodescanner.BarcodeView
import kotlin.reflect.KClass

class QRCodeScannerClass(private val context: Context, private val scanner: BarcodeView, private val editTextPID: EditText) {

    private var isScanning = false
    private var isFlashOn = false
    private var isAutoFocusOn = true

    init {
        scanner.decodeContinuous { result ->
            if (result != null && isScanning) {
                Toast.makeText(context, "Scanned: ${result.text}", Toast.LENGTH_SHORT).show()
                editTextPID.setText(result.text) // Set scanned text to EditText
                stopScanner()
            }
        }
    }
    fun getIsScanning() : Boolean {
        return isScanning;
    }

    fun startScanner() {
        if (!isScanning) {
            scanner.resume()
            isScanning = true
        }
    }

    fun stopScanner() {
        if (isScanning) {
            scanner.pause()
            isScanning = false
        }
    }

    fun releaseScanner() {
        scanner.pause()
        isScanning = false
    }

    fun toggleFlash() {
        isFlashOn = !isFlashOn
        scanner.setTorch(isFlashOn)
    }

    fun toggleAutoFocus() {
        isAutoFocusOn = !isAutoFocusOn
        val settings = scanner.cameraSettings
        settings.isAutoFocusEnabled = isAutoFocusOn
        scanner.cameraSettings = settings
    }
}
