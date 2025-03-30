package com.example.cs320_hospital_and_medical_android_app

import android.app.DatePickerDialog
import android.health.connect.datatypes.units.Length
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private lateinit var updateBtn: Button
    private lateinit var cancelBtn: Button

    //Firebase Initialization
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DEBUG", "You are now in Patient Information Page")

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
        updateBtn = findViewById(R.id.updateBtn)
        cancelBtn = findViewById(R.id.cancelBtn)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        initializeFucntionalities()

        if (userId != null) {
            db.collection("Patients")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if(document.exists()) {
                        getData(userId)
                    }else {
                        submitBtn.setOnClickListener(){
                            pushData(userId, "added")
                        }
                    }
                }
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

    fun editTextAccess(access: Boolean){
        var editTexts = listOf(
            firstNameInput,
            lastNameInput,
            sexInput,
            birthdayInput,
            hmoCompanyInput,
            hmoCardNoInput
        )

        editTexts.forEach { editText ->
            editText.isEnabled = access
            editText.isFocusable = access
            editText.isFocusableInTouchMode = access
            editText.isClickable = access
        }
    }

    fun getData(userId: String) {

        editTextAccess(false)

        submitBtn.visibility = View.INVISIBLE
        cancelBtn.visibility = View.INVISIBLE
        updateBtn.visibility = View.VISIBLE

        db.collection("Patients")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    firstNameInput.setText(document.getString("firstName"))
                    lastNameInput.setText(document.getString("lastName"))
                    sexInput.setText(document.getString("sex"))
                    birthdayInput.setText(document.getString("birthday"))
                    hmoCompanyInput.setText(document.getString("hmoCompany"))
                    hmoCardNoInput.setText(document.getString("hmoCardNo"))

                    updateBtn.setOnClickListener() {

                        editTextAccess(true)

                        submitBtn.visibility = View.VISIBLE
                        cancelBtn.visibility = View.VISIBLE
                        updateBtn.visibility = View.INVISIBLE

                        submitBtn.setOnClickListener() {
                            pushData(userId, "updated")
                            getData(userId)
                        }

                        cancelBtn.setOnClickListener() {
                            getData(userId)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            }
    }

    fun pushData(userId: String, status: String) {
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

        db.collection("Patients")
            .document(userId)
            .set(patient)
            .addOnSuccessListener {
                Toast.makeText(this, "Patient information ${status}!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            }
    }

    fun initializeFucntionalities() {
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


