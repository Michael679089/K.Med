package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QRReaderDisplayPatientInfoActivity : AppCompatActivity() {

    private var userROLE: String? = null  // Use nullable types to handle missing extras
    private var userUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "You are now in QR Reader View - Display patient information")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_reader_view) // Make sure this XML exists!

        // Retrieve extras safely
        userROLE = intent.getStringExtra("ROLE")
        userUID = intent.getStringExtra("UID")

        Log.d("DEBUG", "Received ROLE: $userROLE, UID: $userUID")

        // OPTIONAL: Display values in UI if you have TextViews

    }
}
