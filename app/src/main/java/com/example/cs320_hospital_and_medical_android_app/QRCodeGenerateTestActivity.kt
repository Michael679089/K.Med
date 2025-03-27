package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


private const val QR_SIZE = 1024

class QRCodeGenerateTestActivity : AppCompatActivity() {

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
            val textVal = findViewById<EditText>(R.id.qrTextET).text.toString()

            val qrCodeGenerator = QRCodeGenerator()
            qrCodeGenerator.generateQRCodeToImageView(imageView, textVal, QR_SIZE)
        }
    }
}