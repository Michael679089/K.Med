package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Dashboard : AppCompatActivity() {

    private lateinit var UID : String
    private lateinit var ROLE : String
    private lateinit var NAME : String

    override fun onCreate(savedInstanceState: Bundle?) { // MAIN FUNCTION
        Log.d("DEBUG", "You are now in Dashboard Page")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Load User Info
        UID = intent.getStringExtra("UID") ?: "Unknown"
        ROLE = intent.getStringExtra("ROLE") ?: ""
        NAME = intent.getStringExtra("NAME") ?: ""

        val dbHandler = DBHandlerClass()

        if (ROLE.isBlank()) {
            dbHandler.getRoleOfLoggedInUser(UID) { retrievedRole ->
                if (retrievedRole != "") {
                    ROLE = retrievedRole

                    dbHandler.getNameOfLoggedInUser(UID, retrievedRole) { retrievedName ->
                        if (retrievedName != "") {
                            NAME = retrievedName
                            updateUI()
                        }
                    }
                }
            }
        } else {
            updateUI()
        }
    }

    // # FUNCTIONS

    // ## updating UI
    private fun updateUI() {
        val nameView = findViewById<TextView>(R.id.accountName)
        val idView = findViewById<TextView>(R.id.accountID)
        val qrCode = findViewById<ImageView>(R.id.qrCode)

        nameView.text = NAME
        idView.text = UID
        Log.d("DEBUG", "Final ROLE: $ROLE")

        patientInformation(ROLE, UID)
        loadRoleButtons(ROLE, UID)
        loadScheduleCard(ROLE, UID)

        // Generate QR Code
        val qrGenerator = QRCodeGeneratorClass()
        qrGenerator.generateQRCodeToImageView(qrCode, UID)

        // Adjust Button Section Height
        val buttonSectionContainer = findViewById<FrameLayout>(R.id.buttonSectionContainer)
        val maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, resources.displayMetrics).toInt()
        buttonSectionContainer.post {
            if (buttonSectionContainer.height > maxHeight) {
                buttonSectionContainer.layoutParams.height = maxHeight
                buttonSectionContainer.requestLayout()
            }
        }

        // QR Code Click - Zoom In
        qrCode.setOnClickListener {
            Log.d("DEBUG", "Going to QR zoomed in")

            val rootView = findViewById<ConstraintLayout>(R.id.main)
            val inflater = LayoutInflater.from(this)
            val qrZoomedInView = inflater.inflate(R.layout.qr_zoomed_in, rootView, false)

            // Populate zoomed-in layout
            val qrCodeIV = qrZoomedInView.findViewById<ImageView>(R.id.qrCodeIV)
            val qrZoomedInIDNumber = qrZoomedInView.findViewById<TextView>(R.id.qrZoomedInIDNumber)
            val qrZoomedInUsername = qrZoomedInView.findViewById<TextView>(R.id.qrZoomedInUsername)
            val qrZoomedInRole = qrZoomedInView.findViewById<TextView>(R.id.qrZoomedInRole)

            qrZoomedInIDNumber.text = UID
            qrZoomedInUsername.text = NAME ?: "No Name"
            qrZoomedInRole.text = ROLE ?: "Unknown"

            val qrGenerator = QRCodeGeneratorClass()
            qrGenerator.generateQRCodeToImageView(qrCodeIV, UID)

            val goBackBTN = qrZoomedInView.findViewById<Button>(R.id.qrZoomedInGoBackBTN)
            goBackBTN.setOnClickListener {
                rootView.removeView(qrZoomedInView)
            }

            // Set layout params
            qrZoomedInView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            qrZoomedInView.elevation = 1000f

            // Add overlay
            rootView.addView(qrZoomedInView)
        }
    }


    private fun patientInformation(ROLE: String, PID: String) {
        val editPatientBtn = findViewById<ImageView>(R.id.editPatientBtn)
        editPatientBtn.visibility = View.GONE

        if (ROLE == "patient") {
            editPatientBtn.visibility = View.VISIBLE
            editPatientBtn.setOnClickListener() {
                val intent = Intent(this, PatientInformation::class.java)
                intent.putExtra("uid", PID)
                startActivity(intent)
            }
        }
    }

    // Load RBA Buttons
    private fun loadRoleButtons(ROLE: String, UID: String) {
        val container = findViewById<FrameLayout>(R.id.buttonSectionContainer)
        val inflater = LayoutInflater.from(this)

        val layoutRes = when (ROLE) {
            "patient" -> R.layout.dashboard_buttons_patient
            "doctor" -> R.layout.dashboard_buttons_doctor
            "nurse" -> R.layout.dashboard_buttons_nurse
            else -> null
        }

        layoutRes?.let {
            container.removeAllViews()
            val view = inflater.inflate(it, container, false)
            container.addView(view)

            // Role-based listeners
            when (ROLE) {
                "patient" -> patientButtons(view, UID, ROLE)
                "doctor" -> doctorButtons(view, UID, ROLE)
                "nurse"  -> NurseButtons(view, UID, ROLE)
            }
        }
    }

    // Button Listeners
    private fun patientButtons(view: View, UID: String, ROLE: String) {
        val doctorsBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.scheduleBtn) // Appointment button.
        val prescriptionBtn = view.findViewById<LinearLayout>(R.id.prescriptionBtn)

        doctorsBtn.setOnClickListener {
            val intent = Intent(this, DoctorSchedule::class.java)
            intent.putExtra("ROLE", ROLE)
            intent.putExtra("UID", UID)
            startActivity(intent)
        }

        scheduleBtn.setOnClickListener {
            val intent = Intent(this, PatientAppointment::class.java)
            intent.putExtra("UID", UID)
            startActivity(intent)
        }

        prescriptionBtn.setOnClickListener {
            startActivity(Intent(this, Prescription::class.java))
        }
    }

    private fun doctorButtons(view: View, UID: String, ROLE: String) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.doctorAccessSchedule)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("ROLE", ROLE)
            startActivity(intent)
        }

        scheduleBtn.setOnClickListener {
            val intent = Intent(this, DoctorSchedule::class.java)
            intent.putExtra("ROLE", ROLE)
            intent.putExtra("UID", UID)
            startActivity(intent)
        }
    }

    private fun NurseButtons(view: View, UID: String, ROLE: String) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val doctorBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("ROLE", ROLE)
            intent.putExtra("UID", UID)
            startActivity(intent)
        }

        doctorBtn.setOnClickListener {
            val intent = Intent(this, DoctorSchedule::class.java)
            intent.putExtra("ROLE", ROLE)
            startActivity(intent)
        }
    }

    // Load Schedule Card
    private fun loadScheduleCard(ROLE: String, UID: String) {
        val scheduleContainer = findViewById<LinearLayout>(R.id.scheduleCardContent)
        val inflater = LayoutInflater.from(this)
        val db = FirebaseFirestore.getInstance()

        val setScheduleLayout: (Int, Map<Int, String?>) -> Unit = { layoutRes, textMap ->
            val view = inflater.inflate(layoutRes, scheduleContainer, false)
            textMap.forEach { (id, value) ->
                view.findViewById<TextView?>(id)?.text = value ?: ""
            }

            scheduleContainer.removeAllViews()
            scheduleContainer.addView(view)
        }

        Log.d("DEBUG", "Displaying schedule cards")
        when (ROLE) {
            // Patient Schedule Card
            "patient" -> {
                db.collection("appointments")
                    .whereEqualTo("patientID", UID)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .addSnapshotListener { documents, exception ->
                        if (exception != null) {
                            Log.e("FIRESTORE", "Failed to fetch patient appointment", exception)
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                            return@addSnapshotListener
                        }

                        if (documents != null && !documents.isEmpty) {
                            val doc = documents.first()
                            val status = doc.getString("status") ?: "none"
                            val date = doc.getString("date")
                            val today = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(Date())

                            Log.d("LOAD_CARD_DEBUG", "Loading appointment for UID: $UID")
                            Log.d("DATE_DEBUG", "today = $today, appointment date = $date")
                            Log.d("FIRESTORE", "Fetched doc with status: $status and date: $date")

                            when {
                                // FIXED: Check if status is 'booked' and date is TODAY
                                status == "booked" && date == today -> {

                                    val view = inflater.inflate(R.layout.dashboard_schedule_patient_booked, scheduleContainer, false)

                                    view.findViewById<TextView>(R.id.textAppointmentDate)?.text = date
                                    view.findViewById<TextView>(R.id.doctorName)?.text = "Dr. ${doc.getString("doctorName")}"

                                    val confirmBtn = view.findViewById<Button>(R.id.confirmArrivalBtn)
                                    confirmBtn.setOnClickListener {
                                        val appointmentId = doc.id
                                        val patientName = doc.getString("patientName") ?: "Unknown"
                                        val nurseUid = "NID234567891"

                                        db.collection("appointments").document(appointmentId)
                                            .update(
                                                mapOf(
                                                    "status" to "queue_onboarding",
                                                    "queueNumber" to 1,
                                                    "queueType" to "onboarding",
                                                    "readyToCall" to false
                                                )
                                            )
                                            .addOnSuccessListener {
                                                db.collection("assignments").document()
                                                    .set(
                                                        mapOf(
                                                            "nurseId" to nurseUid,
                                                            "hasTask" to true,
                                                            "patientName" to patientName,
                                                            "appointmentId" to appointmentId
                                                        )
                                                    )
                                                Toast.makeText(this, "Arrival confirmed!", Toast.LENGTH_SHORT).show()
                                                loadScheduleCard("patient", UID)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Failed to confirm arrival", Toast.LENGTH_SHORT).show()
                                                Log.e("FIRESTORE", "Error updating appointment", it)
                                            }
                                    }

                                    scheduleContainer.removeAllViews()
                                    scheduleContainer.addView(view)
                                }

                                status == "queue_onboarding" && date == today -> {
                                    setScheduleLayout(
                                        R.layout.dashboard_schedule_patient_queue,
                                        mapOf(
                                            R.id.textQueueLocation to "Nurse Station",
                                            R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                        )
                                    )
                                }

                                status == "queue_doctor" && date == today -> {
                                    setScheduleLayout(
                                        R.layout.dashboard_schedule_patient_queue,
                                        mapOf(
                                            R.id.textQueueLocation to "ROOM 504",
                                            R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                        )
                                    )
                                }

                                else -> {
                                    setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                                }
                            }
                        } else {
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                        }
                    }
            }

            // Nurse Schedule Card
            "nurse" -> {
                db.collection("assignments")
                    .whereEqualTo("nurseId", UID)
                    .addSnapshotListener { documents, exception ->
                        if (exception != null) {
                            Log.e("FIRESTORE", "Failed to fetch nurse assignment", exception)
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                            return@addSnapshotListener
                        }

                        if (documents != null && documents.getBoolean("hasTask") == true) {
                            val inflater = LayoutInflater.from(this)
                            val view = inflater.inflate(R.layout.dashboard_schedule_nurse, null)

                            val patientName = documents.getString("patientName") ?: "Unknown"
                            val appointmentId = documents.getString("appointmentId") ?: ""

                            view.findViewById<TextView>(R.id.patientName)?.text = patientName

                            val callBtn = view.findViewById<Button>(R.id.callPatient)
                            val exitBtn = view.findViewById<Button>(R.id.exitPatient)

                            // Call Button
                            callBtn.setOnClickListener {
                                db.collection("appointments").document(appointmentId)
                                    .update(
                                        mapOf(
                                            "status" to "queue_doctor",
                                            "queueType" to "doctor",
                                            "readyToCall" to true
                                        )
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Patient forwarded to doctor.", Toast.LENGTH_SHORT).show()
                                        // Remove task from nurse
                                        db.collection("assignments").document(UID)
                                            .delete()
                                        loadScheduleCard("nurse", UID)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to update appointment.", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            // Exit Button
                            exitBtn.setOnClickListener {
                                db.collection("appointments").document(appointmentId)
                                    .update(mapOf("status" to "done"))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Patient marked as exited.", Toast.LENGTH_SHORT).show()
                                        db.collection("assignments").document(UID)
                                            .delete()
                                        loadScheduleCard("nurse", UID)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to complete exit.", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            val scheduleContainer = findViewById<LinearLayout>(R.id.scheduleCardContent)
                            scheduleContainer.removeAllViews()
                            scheduleContainer.addView(view)

                        } else {
                            Log.e("FIRESTORE", "No documents found for this nurseId")
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                        }
                    }
            }



            // Doctor Schedule Card
            "doctor" -> {
                val today = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(Date())

                db.collection("appointments")
                    .whereEqualTo("doctorID", UID)
                    .whereEqualTo("status", "queue_doctor")
                    .whereEqualTo("readyToCall", true)
                    .whereEqualTo("date", today)
                    .limit(1)
                    .addSnapshotListener { documents, exception ->
                        if (exception != null) {
                            Log.e("FIRESTORE", "Failed to fetch doctor queue", exception)
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                            return@addSnapshotListener
                        }

                        if (documents != null && !documents.isEmpty) {
                            val doc = documents.first()

                            val appointmentId = doc.id
                            val patientName = doc.getString("patientName") ?: "Unknown"

                            val inflater = LayoutInflater.from(this)
                            val view = inflater.inflate(R.layout.dashboard_schedule_doctor, null)

                            view.findViewById<TextView>(R.id.patientName)?.text = patientName

                            val callBtn = view.findViewById<Button>(R.id.callPatient_doctor)
                            callBtn.setOnClickListener {
                                // Mark appointment done or handled
                                db.collection("appointments").document(appointmentId)
                                    .update(mapOf("status" to "done"))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Appointment completed.", Toast.LENGTH_SHORT).show()
                                        loadScheduleCard("doctor", UID)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to complete appointment.", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            val scheduleContainer = findViewById<LinearLayout>(R.id.scheduleCardContent)
                            scheduleContainer.removeAllViews()
                            scheduleContainer.addView(view)
                        } else {
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                        }
                    }
            }

            // No Schedule View for Patient, Doctor & Nurse
            else -> {
                Log.d("FIRESTORE", "No appointment document found for $UID")
                setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
            }
        }
    }
}