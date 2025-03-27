package com.example.cs320_hospital_and_medical_android_app

import android.content.Context
import android.widget.Toast
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView

class QRCodeScanner(private val context: Context, private val scanner: BarcodeView) {

    private var isScanning = false

    private var isFlashOn = false
    private var isAutoFocusOn = true

    init { // runs automatically once class created
        scanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result != null && isScanning) {
                    Toast.makeText(context, "Scanned: ${result.text}", Toast.LENGTH_SHORT).show()
                    stopScanner()
                }
            }
        })
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

    fun releaseScanner() { // to fully release sources from scanner.
        scanner.pause()
        isScanning = false
    }


    fun isScanning(): Boolean {
        return isScanning
    }

    // # Additional Functionality: Toggle Flash and AutoFocus

    fun toggleFlash() {
        isFlashOn = !isFlashOn
        scanner.setTorch(isFlashOn) // Turns flashlight on/off
    }

    fun toggleAutoFocus() {
        isAutoFocusOn = !isAutoFocusOn
        val settings = scanner.cameraSettings
        settings.isAutoFocusEnabled = isAutoFocusOn
        scanner.cameraSettings = settings // Apply new settings
    }
}
