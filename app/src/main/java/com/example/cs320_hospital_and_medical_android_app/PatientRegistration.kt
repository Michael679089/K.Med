package com.example.cs320_hospital_and_medical_android_app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import java.util.Calendar

class PatientRegistration : AppCompatActivity() {

    private lateinit var  patientInfo: LinearLayout
    private lateinit var  accountInfo: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_registration)

        val birthdayEditText = findViewById<EditText>(R.id.birthday)

        birthdayEditText.setOnClickListener {
            // Get the current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show the DatePickerDialog
            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Set the selected date in the EditText
                    val formattedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                    birthdayEditText.setText(formattedDate)
                }, year, month, day)
            datePickerDialog.show()
        }

        patientInfo = findViewById(R.id.patientInfo)
        val nextBtn: Button = findViewById(R.id.nextBtn)

        nextBtn.setOnClickListener() {
            changeLayout(accountInfo)
        }

        accountInfo = findViewById(R.id.accountInfo)
        val registerBtn: Button = findViewById(R.id.registerBtn)

        registerBtn.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun changeLayout(stepLayout: LinearLayout) {
        patientInfo.visibility = View.GONE
        accountInfo.visibility = View.GONE
        stepLayout.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        when {
            accountInfo.isVisible -> {
                changeLayout(patientInfo)

            }
            else -> super.onBackPressed()
        }
    }
}