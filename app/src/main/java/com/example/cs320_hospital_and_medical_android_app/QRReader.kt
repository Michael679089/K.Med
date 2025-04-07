package com.example.cs320_hospital_and_medical_android_app

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.BarcodeView
// imports Related to camera permission
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView


class QRReader : AppCompatActivity() {

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String> // this is the permission launcher
    private lateinit var qrScanner: QRCodeScannerClass

    private lateinit var ROLE : String
    private lateinit var UID : String

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("DEBUG", "You are now in QR Reader View")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.qr_reader)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the Intent Extra Information
        ROLE = intent.getStringExtra("ROLE").toString()
        UID = intent.getStringExtra("UID").toString()
        val patientID = intent.getStringExtra("patient_PID") ?: null

        if (patientID != null && patientID.isNotEmpty()) {
            openQRReaderDisplayPatientInfoActivity(patientID)
        }

        if (ROLE == "patient") {
            val intent = Intent(this, Dashboard::class.java)
            intent.putExtra("ROLE", ROLE)
            intent.putExtra("UID", UID)
            startActivity(intent)
        }

        // Initialize permission launcher
        initPermissionLauncher()

        setContentView(R.layout.qr_reader)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // I want to pause everything during the cameraPermission.
        Log.d("DEBUG", "Pause everything, ask camera permission first.")
        if (checkCameraPermission()) {
            // Check Permission is asynchronous, this code runs only after user input camera permission.
            Log.d("DEBUG", "Camera permission already granted ✅")
            startQRScannerFunction()
        } else {
            requestPermission()
        }

        val qrFrameContainer: FrameLayout = findViewById(R.id.qrFrameContainer)
        qrFrameContainer.setOnClickListener {
            Log.d("DEBUG", "QRFrameScanner clicked")
            if (checkCameraPermission()) {
                if (!qrScanner.getIsScanning()) {
                    qrScanner.startScanner()
                }
            } else {
                openAppSettings()
                requestPermission()
            }
        }
        // Ensure width = height. Ratio 1:1
        qrFrameContainer.viewTreeObserver.addOnGlobalLayoutListener {
            qrFrameContainer.layoutParams.height = qrFrameContainer.width
            qrFrameContainer.requestLayout() // Apply changes
        }
    }



    // # QR READING FUNCTIONS

    private fun startQRScannerFunction() { // ## The Function after permission.
        Log.d("DEBUG", "Camera Permission finished, unpause everything.")

        val isInAppointment : Boolean = intent.getStringExtra("InAppointment").toBoolean()

        if (isInAppointment) {
            val prevPatientID = intent.getStringExtra("patient_PID").toString()
            openQRReaderDisplayPatientInfoActivity(prevPatientID)
        }
        else {
            val qrCodeScanner: BarcodeView = findViewById(R.id.qrBarcodeScanner)
            val editTextPid: EditText = findViewById(R.id.editTextPid)

            qrScanner = QRCodeScannerClass(this, qrCodeScanner, editTextPid)
            qrScanner.startScanner()
            Log.d("DEBUG", "⌚ Scanner starts Scanning")

            val qrFrameContainer: FrameLayout = findViewById(R.id.qrFrameContainer)
            qrFrameContainer.setOnClickListener {
                Log.d("DEBUG", "QRFrameScanner clicked")
                if (checkCameraPermission()) {
                    if (!qrScanner.getIsScanning()) {
                        qrScanner.startScanner()
                    }
                } else {
                    openAppSettings()
                    requestPermission()
                }
            }

            val btnToggleFlash: ImageButton = findViewById(R.id.btnToggleFlash)
            btnToggleFlash.setOnClickListener {
                qrScanner.toggleFlash()
            }
            val btnToggleAutoFocus: ImageButton = findViewById(R.id.btnToggleAutoFocus)
            btnToggleAutoFocus.setOnClickListener {
                qrScanner.toggleAutoFocus()
            }

            val btnSubmit: Button = findViewById(R.id.btnSubmit)
            btnSubmit.setOnClickListener {
                val editTextPIDValue = editTextPid.text.toString().trim()
                if (editTextPIDValue.isNotEmpty()) {
                    openQRReaderDisplayPatientInfoActivity(editTextPIDValue)
                }
                else {
                    Log.d("DEBUG", "ERROR: Input is empty or null")
                    Toast.makeText(this, "Input is empty or null", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openQRReaderDisplayPatientInfoActivity(editTextPIDValue: String) { // ## The Function after scanning.
        setContentView(R.layout.qr_reader_view)

        // Get the latest Appointment (⏰ asynchronous function)
        val dbHandler = DBHandlerClass()
        dbHandler.getLatestAppointment(editTextPIDValue) { latestAppointment -> // ⏰ asynchronous function
            if (latestAppointment != null) { // appointment is not null
                Log.d("DEBUG", "Result from new object dbhandler: $latestAppointment")

                // getting the values from firestore db
                val patientNameVal = latestAppointment["patientName"] as? String
                val patientIDVal = latestAppointment["patientID"] as? String
                val doctorNameVal = latestAppointment["doctorName"] as? String
                var scheduledDateVal = latestAppointment["date"] as? String
                scheduledDateVal += " " + latestAppointment["time"] as? String
                val reasonVal = latestAppointment["reason"] as? String

                // setting the text
                val patientNameTextView : TextView = findViewById(R.id.patientName)
                patientNameTextView.text = patientNameVal
                val patientIDTextView : TextView = findViewById(R.id.patientID)
                patientIDTextView.text = patientIDVal
                val doctorNameTextView : TextView = findViewById(R.id.doctorName)
                doctorNameTextView.text = doctorNameVal
                val doctorScheduleTextView : TextView = findViewById(R.id.doctorSchedule)
                doctorScheduleTextView.text = scheduledDateVal
                val reasonForConsultationTextView : TextView = findViewById(R.id.patientSymptom)
                reasonForConsultationTextView.text = reasonVal

                // continue function from here 🚩
                val bloodPressureET : EditText = findViewById(R.id.editBloodPressure)
                val weightInPoundsET : EditText = findViewById(R.id.editWeight)

                if (ROLE == "nurse") {
                    Log.d("DEBUG", "you are nurse")
                    val nurseSubmitBTN : Button = findViewById(R.id.btnNurseSubmit)
                    nurseSubmitBTN.setOnClickListener {
                        val bloodPressureETVal = bloodPressureET.text.toString().toDoubleOrNull()
                        val weightInPoundsETVal = weightInPoundsET.text.toString().toDoubleOrNull()

                        // check if they are numbers.
                        if (bloodPressureETVal == null || weightInPoundsETVal == null) {
                            Toast.makeText(this, "Input all fields", Toast.LENGTH_LONG).show()
                        } else {
                            dbHandler.updateNurseTask(UID, patientIDVal.toString(), bloodPressureETVal, weightInPoundsETVal)
                            val intent = Intent(this, Dashboard::class.java)
                            intent.putExtra("ROLE", ROLE)
                            intent.putExtra("UID", UID)
                            startActivity(intent)
                        }
                    }
                }
                else if (ROLE == "doctor") {
                    Log.d("DEBUG", "you are doctor")
                    // android:visibility == gone for: Blood Pressure and Weight Edit Text (Disable Nurse-specific UI elements):
                    val bloodPressureWeightInstructionTextView : TextView = findViewById(R.id.textView4)
                    bloodPressureWeightInstructionTextView.visibility = View.GONE
                    bloodPressureET.visibility = View.GONE
                    weightInPoundsET.visibility = View.GONE

                    // android:visibility == gone for: Remove Queue to Doctor Button from Context
                    val nurseSubmitBTN : Button = findViewById(R.id.btnNurseSubmit)
                    nurseSubmitBTN.visibility = View.GONE

                    // switch android:visibility to the following (Doctor-specific UI elements):
                    val doctorVitalsContainerLinearLayout : LinearLayout = findViewById(R.id.doctorVitalsContainer)
                    val doctorButtonContainerLinearLayout : LinearLayout = findViewById(R.id.doctorButtonContainer)
                    doctorVitalsContainerLinearLayout.visibility = View.VISIBLE
                    doctorButtonContainerLinearLayout.visibility = View.VISIBLE

                    // Inflate qr_reader_button_doctor.xml and add it to doctorButtonContainerLinearLayout
                    val inflater = LayoutInflater.from(this)
                    val qrReaderButtonView = inflater.inflate(R.layout.qr_reader_button_doctor, doctorButtonContainerLinearLayout, false)
                    doctorButtonContainerLinearLayout.addView(qrReaderButtonView)


                    // Inside inflated layout
                    val prescriptionBTN : Button = findViewById(R.id.btnSubmitDoctor)
                    prescriptionBTN.setOnClickListener {
                        val intent = Intent(this, Prescription::class.java)
                        intent.putExtra("ROLE", ROLE)
                        intent.putExtra("UID", UID)
                        intent.putExtra("patientId", patientIDVal)
                        intent.putExtra("patientName", patientNameVal)
                        intent.putExtra("InAppointment", true)
                        startActivity(intent)
                    }


                    val forExitBTN : Button = findViewById(R.id.btnDoctorAction)
                    forExitBTN.setOnClickListener {
                        Toast.makeText(this, "Patient for Exit", Toast.LENGTH_SHORT).show()
                        dbHandler.updateAppointment(UID, patientIDVal.toString())
                        val intent = Intent(this, Dashboard::class.java)
                        intent.putExtra("ROLE", ROLE)
                        intent.putExtra("UID", UID)
                        startActivity(intent)
                    }

                }
            }
            else { // if no appointments found, return user to dashboard.
                Toast.makeText(this, "No appointments found.", Toast.LENGTH_SHORT).show()
                Log.d("DEBUG", "ERROR: No appointments found.")

                val intent = Intent(this, Dashboard::class.java)
                intent.putExtra("ROLE", ROLE)
                intent.putExtra("UID", UID)
                startActivity(intent)
            }
        }

    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun initPermissionLauncher() { // ## initializes camera permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("DEBUG", "Camera permission granted ✅")
                startQRScannerFunction()
            } else {
                Log.e("DEBUG", "Camera permission denied ❌")
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Tap the QR Scanner to open permission settings!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission() {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    // ## Override onResume to check permission after the user returns from settings
    override fun onResume() {
        super.onResume()

        // Check if camera permission is granted when the user returns from settings
        if (checkCameraPermission() && !qrScanner.getIsScanning()) {
            // If permission is granted, start the scanner
            startQRScannerFunction()
        }
    }
}

