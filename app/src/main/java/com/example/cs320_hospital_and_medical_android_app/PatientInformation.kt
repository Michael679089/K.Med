package com.example.cs320_hospital_and_medical_android_app

import android.app.DatePickerDialog
import android.health.connect.datatypes.units.Length
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale


class PatientInformation : AppCompatActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var sexInput: AutoCompleteTextView
    private lateinit var birthdayInput: EditText
    private lateinit var hmoCompanyInput:EditText
    private lateinit var hmoCardNoInput: EditText

    private lateinit var submitBtn: Button

    //Firebase Initialization
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        //Firebase Initialization
        db = FirebaseFirestore.getInstance()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_information)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        sexInput = findViewById(R.id.sexInput)
        birthdayInput = findViewById(R.id.birthdayInput)
        hmoCompanyInput = findViewById(R.id.hmoCompanyInput)
        hmoCardNoInput = findViewById(R.id.hmoCardNoInput)

        submitBtn = findViewById(R.id.submitBtn)

        initializeFucntionalities()

        submitBtn.setOnClickListener(){
            insertDataToFirestore()
        }
    }

    data class Patient (
        var firstName: String? = null,
        var lastName: String? = null,
        var sex: String? = null,
        var birthday: String? = null,
        var hmoCompany: String? = null,
        var hmoCardNo: String? = null
    )

    fun insertDataToFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        var firstName = firstNameInput.text.toString()
        val lastName = lastNameInput.text.toString()
        val sex = sexInput.text.toString()
        val birthday = birthdayInput.text.toString()
        val hmoCompany = hmoCompanyInput.text.toString()
        val hmoCardNo = hmoCardNoInput.text.toString()

        val patient = Patient(
            firstName = firstName,
            lastName = lastName,
            sex = sex,
            birthday = birthday,
            hmoCompany = hmoCompany,
            hmoCardNo = hmoCardNo
        )

        if (userId != null) {
            db.collection("Patients")
                .document(userId)
                .set(patient)
                .addOnSuccessListener {
                    Toast.makeText(this, "Patient information added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun initializeFucntionalities(){
        //Dropdown for Sex Input

        val sexOptions = listOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexOptions)
        sexInput.setAdapter(adapter)
        sexInput.setOnClickListener{sexInput.showDropDown()}

        //Calendar Input
        birthdayInput.setOnClickListener {
            // Get the current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePicker
            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Calendar with the selected Date
                    val selectedDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }

                    // Format to January 1, 1999
                    val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val formattedDate = formatter.format(selectedDate.time)

                    // Display the selected date
                    birthdayInput.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePicker.show()
        }
    }
}