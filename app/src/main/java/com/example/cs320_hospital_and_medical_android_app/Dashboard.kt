package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) { // MAIN FUNCTION
        Log.d("DEBUG", "You are now in Dashboard Page")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Load User Info
        val ROLE = intent.getStringExtra("ROLE") ?: return
        val NAME = intent.getStringExtra("NAME") ?: "No Name"
        val UID = intent.getStringExtra("UID") ?: "Unknown"

        val nameView = findViewById<TextView>(R.id.accountName)
        val idView = findViewById<TextView>(R.id.accountID)
        val qrCode = findViewById<ImageView>(R.id.qrCode)
        val settings = findViewById<ImageView>(R.id.settings)

        Log.d("DEBUG", ROLE)

        val qrGenerator = QRCodeGeneratorClass()
        qrGenerator.generateQRCodeToImageView(qrCode, UID)

        nameView.text = NAME
        idView.text = UID

        patientInformation(ROLE, UID)
        loadRoleButtons(ROLE, UID)
        loadScheduleCard(ROLE, UID)

        settings.setOnClickListener() {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val buttonSectionContainer = findViewById<FrameLayout>(R.id.buttonSectionContainer)
        val maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, resources.displayMetrics).toInt()
        buttonSectionContainer.post {
            if (buttonSectionContainer.height > maxHeight) { // Hardcoded max height in pixels
                buttonSectionContainer.layoutParams.height = maxHeight
                buttonSectionContainer.requestLayout()
            }
        }

        // QR Code - Zoomed In - Overlay
        qrCode.setOnClickListener {
            Log.d("DEBUG", "Going to qr zoomed in")

            // Ensure the root layout is a FrameLayout (we want to stack views)
            val rootView = findViewById<ConstraintLayout>(R.id.main)
            val inflater = LayoutInflater.from(this)
            val qrZoomedInView = inflater.inflate(R.layout.qr_zoomed_in, rootView, false)

            // Populate data in the zoomed-in layout
            val qrCodeIV = qrZoomedInView.findViewById<ImageView>(R.id.qrCodeIV)
            qrGenerator.generateQRCodeToImageView(qrCodeIV, UID)

            val qrZoomedInIDNumber = qrZoomedInView.findViewById<TextView>(R.id.qrZoomedInIDNumber)
            qrZoomedInIDNumber.text = UID

            val qrZoomedInUsername = qrZoomedInView.findViewById<TextView>(R.id.qrZoomedInUsername)
            qrZoomedInUsername.text = NAME

            val qrZoomedInRole = qrZoomedInView.findViewById<TextView>(R.id.qrZoomedInRole)
            qrZoomedInRole.text = ROLE

            val goBackBTN = qrZoomedInView.findViewById<Button>(R.id.qrZoomedInGoBackBTN)
            goBackBTN.setOnClickListener {
                rootView.removeView(qrZoomedInView)  // Remove the overlay when clicking "Go Back"
            }

            // Set layout params to ensure it covers the full screen
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            qrZoomedInView.layoutParams = params

            // Set the elevation (z-index) to ensure it's on top of other views
            qrZoomedInView.elevation = 1000f  // You can adjust this value for desired stacking order

            // Add the zoomed-in view on top of the dashboard
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
                intent.putExtra("UID", PID)
                intent.putExtra("ROLE", ROLE)
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
                "patient" -> PatientButtons(view, UID, ROLE)
                "doctor" -> DoctorButtons(view, UID, ROLE)
                "nurse"  -> NurseButtons(view, UID, ROLE)
            }
        }
    }

    // Button Listeners
    private fun PatientButtons(view: View, UID: String, ROLE: String) {
        val doctorBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.scheduleBtn)
        val prescriptionBtn = view.findViewById<LinearLayout>(R.id.prescriptionBtn)

        doctorBtn.setOnClickListener {
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
            val intent = Intent(this, Prescription::class.java)
            intent.putExtra("ROLE", ROLE)
            intent.putExtra("UID", UID)
            startActivity(intent)
        }
    }

    private fun DoctorButtons(view: View, UID: String, ROLE: String) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.doctorAccessSchedule)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("ROLE", ROLE)
            intent.putExtra("UID", UID)
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
                                                    "queueStation" to "Onboarding Desk",
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
                                            R.id.textQueueLocation to doc.get("queueStation")?.toString(),
                                            R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                        )
                                    )
                                }

                                status == "queue_nurse" && date == today -> {
                                    setScheduleLayout(
                                        R.layout.dashboard_schedule_patient_queue,
                                        mapOf(
                                            R.id.textQueueLocation to doc.get("queueStation")?.toString(),
                                            R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                        )
                                    )
                                }

                                status == "queue_doctor" && date == today -> {
                                    setScheduleLayout(
                                        R.layout.dashboard_schedule_patient_queue,
                                        mapOf(
                                            R.id.textQueueLocation to doc.get("queueStation")?.toString(),
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

                        if (documents != null && !documents.isEmpty) {

                            val doc = documents.first()

                            if (doc.getBoolean("hasTask") == true) {
                                val inflater = LayoutInflater.from(this)
                                val view = inflater.inflate(R.layout.dashboard_schedule_nurse, null)

                                val patientName = doc.getString("patientName") ?: "Unknown"
                                val appointmentId = doc.getString("appointmentId") ?: ""

                                view.findViewById<TextView>(R.id.patientName)?.text = patientName

                                val callBtn = view.findViewById<Button>(R.id.callPatient)
                                val exitBtn = view.findViewById<Button>(R.id.exitPatient)

                                // Call Button
                                callBtn.setOnClickListener {
                                    db.collection("appointments").document(appointmentId)
                                        .update(
                                            mapOf(
                                                "status" to "queue_nurse",
                                                "queueNumber" to 1,
                                                "queueStation" to "Nurse Station",
                                            )
                                        )
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Patient called to nurse station.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to call the patient.", Toast.LENGTH_SHORT).show()
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
                            }else {
                                Log.e("FIRESTORE", "No documents found for this nurseId")
                                setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                            }
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