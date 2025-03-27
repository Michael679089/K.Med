package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.WriterException
import com.google.zxing.BarcodeFormat

private const val TAG = "MainActivity"
private const val QR_SIZE = 1024

class GenerateQRCodeTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_generate_qrcode_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val generateQRCodeBTN = findViewById<Button>(R.id.generateQRBTN)

        generateQRCodeBTN.setOnClickListener {
            val imageView = findViewById<ImageView>(R.id.qrImageView)

            generateQrCodeToImageView(imageView)
        }

    }

    private fun generateQrCodeToImageView(imageView: ImageView) {
        val inputTextET = findViewById<EditText>(R.id.qrTextET)
        val inputText = inputTextET.text.toString()

        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(inputText, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
            imageView.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            Log.e(TAG, "generateQrCode: ${e.message}")
        }
    }


}