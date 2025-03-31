package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DoctorSchedule : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_schedule)

        viewFlipper = findViewById(R.id.viewFlipper)
        val doctorSchedule: ImageView = findViewById(R.id.doctorSchedule)

        doctorSchedule.setOnClickListener(){
            viewDoctorSchedule()
        }

    }

    private fun viewDoctorSchedule() {
        viewFlipper.displayedChild = 1

        val btnSetSchedule: Button = findViewById(R.id.btnSetSchedule)
        btnSetSchedule.setOnClickListener() {
            scheduleForm()
        }

        val deleteBtn: ImageView = findViewById(R.id.deleteBtn)
        deleteBtn.setOnClickListener(){
            createToast("Deleted")
        }
    }

    private fun scheduleForm(){
        viewFlipper.displayedChild = 2

        val setBtn: Button = findViewById(R.id.setBtn)
        setBtn.setOnClickListener() {
            createToast("Scheduled")
            viewDoctorSchedule()
        }

        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener() {
            viewDoctorSchedule()
        }
    }

    private fun createToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (viewFlipper.displayedChild > 0){
            viewFlipper.showPrevious()
        } else {
            super.onBackPressed()
        }
    }
}