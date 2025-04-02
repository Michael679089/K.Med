package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Prescription : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prescription)

        viewFlipper = findViewById(R.id.viewFlipper)

        val viewPrescriptionBtn: ImageView = findViewById(R.id.viewPrescriptionBtn)
        viewPrescriptionBtn.setOnClickListener() {
            viewPrescription()
        }

        val addPrescriptionBtn: ImageButton = findViewById(R.id.addPrescriptionBtn)
        addPrescriptionBtn.setOnClickListener() {
            prescriptionForm()
        }
    }

    private fun viewPrescription() {
        viewFlipper.displayedChild = 1

        val btnEditPrescription: ImageButton = findViewById(R.id.btnEditPrescription)
        btnEditPrescription.setOnClickListener() {
            prescriptionForm()
        }

        val btnDeletePrescription: ImageButton = findViewById(R.id.btnDeletePrescription)
        btnDeletePrescription.setOnClickListener(){
            createToast("Deleted")
            listPrescription()
        }
    }

    private fun prescriptionForm(){
        viewFlipper.displayedChild = 2

        val publishBtn: Button = findViewById(R.id.publishBtn)
        publishBtn.setOnClickListener() {
            createToast("Published")
            listPrescription()
        }

        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener() {
            listPrescription()
        }
    }

    private fun listPrescription() {
        viewFlipper.displayedChild = 0
    }

    private fun createToast(message: String) {
       Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (viewFlipper.displayedChild > 0){
            listPrescription()
        } else {
            super.onBackPressed()
        }
    }
}