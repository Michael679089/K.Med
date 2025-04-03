package com.example.cs320_hospital_and_medical_android_app

import android.app.DatePickerDialog
import android.content.Intent
import android.health.connect.datatypes.units.Length
import android.icu.util.Calendar
import android.os.Bundle
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
import com.google.firebase.firestore.AggregateSource


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
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        //Firebase Initialization
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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

        //Fetching role
        val ROLE = intent.getStringExtra("ROLE") ?: "patient"

        val userId = intent.getStringExtra("uid") ?: "Unknown"

        initializeFucntionalities()

        if (userId != null) {
            db.collection("Patients")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if(document.exists()) {
                        getPatient(userId)
                    }else {
                        submitBtn.setOnClickListener(){
                            addPatient(userId, "added", ROLE)
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

    fun getPatient(UID: String) {

        editTextAccess(false)

        submitBtn.visibility = View.INVISIBLE
        cancelBtn.visibility = View.INVISIBLE
        updateBtn.visibility = View.VISIBLE

        db.collection("Patients")
            .document(UID)
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
                            addPatient(UID, "updated", "")
                            getPatient(UID)
                        }

                        cancelBtn.setOnClickListener() {
                            getPatient(UID)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            }
    }

    fun addPatient(UID: String, status: String, ROLE: String) {

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

        if (ROLE == "patient-notreg") {
            val patientCollection = db.collection("Patients")
            patientCollection.count().get(AggregateSource.SERVER)
                .addOnSuccessListener { snapshot ->
                    var countPatients = snapshot.count.toInt()
                    val formatNumber = String.format("PID%08d", countPatients + 1)
                    var ASSIGN_UID = formatNumber

                    val userData = mapOf("accountId" to ASSIGN_UID, "role" to "patient")
                    val userCollection = db.collection("users")
                    auth.uid?.let {
                        userCollection.document(it)
                            .set(userData)

                        db.collection("Patients")
                            .document(ASSIGN_UID)
                            .set(patient)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Patient information ${status}!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Dashboard::class.java)
                                intent.putExtra("ROLE", "patient")
                                intent.putExtra("UID", ASSIGN_UID)
                                intent.putExtra("NAME", "${firstName} ${lastName}")

                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        } else {
            db.collection("Patients")
                .document(UID)
                .set(patient)
                .addOnSuccessListener {
                    Toast.makeText(this, "Patient information ${status}!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
                }
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

