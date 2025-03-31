package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

class PatientAppointment : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appointments)

        viewFlipper = findViewById(R.id.viewFlipper)

        val addAppointmentBtn: ImageButton = findViewById(R.id.addAppointmentBtn)
        addAppointmentBtn.setOnClickListener(){
            appointmentForm()
        }

        val deleteBtn: ImageView = findViewById(R.id.deleteBtn)
        deleteBtn.setOnClickListener() {
            createToast("Deleted!")
        }
    }

    private fun appointmentForm(){
        viewFlipper.displayedChild = 1

        val bookBtn: Button = findViewById(R.id.bookBtn)
        bookBtn.setOnClickListener() {
            createToast("Scheduled!")
            listAppointment()
        }

        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener() {
            listAppointment()
        }
    }

    private fun listAppointment() {
        viewFlipper.displayedChild = 0
    }

    private fun createToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (viewFlipper.displayedChild > 0){
            listAppointment()
        } else {
            super.onBackPressed()
        }
    }

}