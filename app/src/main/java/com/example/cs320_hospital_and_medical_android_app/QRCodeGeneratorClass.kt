package com.example.cs320_hospital_and_medical_android_app

import android.util.Log
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

class QRCodeGeneratorClass public constructor() {
     fun generateQRCodeToImageView(imageView: ImageView, textValue : String, qrSIZE : Int) {
        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(textValue, BarcodeFormat.QR_CODE, qrSIZE, qrSIZE)
            imageView.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            Log.e("DEBUG", "QRCodeGenerator Error: generateQrCode: ${e.message}")
        }
    }
}