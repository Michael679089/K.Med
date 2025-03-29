package com.example.cs320_hospital_and_medical_android_app

import android.content.Context
import android.util.Log
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView

class QRCodeScanner(private val context: Context, private val scanner: BarcodeView, private val listener: QRCodeScanListener) {

    private var isScanning = false
    private var isFlashOn = false
    private var isAutoFocusOn = true

    fun startScanner() {
        if (!isScanning) {
            scanner.resume()
            scanner.decodeContinuous { result ->
                if (result != null) {
                    listener.onQRCodeScanned(result.text) // Pass result to activity
                }
            }
            isScanning = true

        }
    }

    fun getIsScanning() : Boolean {
        return isScanning;
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

    interface QRCodeScanListener {
        fun onQRCodeScanned(result: String)
    }
}
