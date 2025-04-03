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
import android.widget.ImageView
import android.view.View
import android.widget.LinearLayout


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

        Log.d("DEBUG", ROLE)

        val qrGenerator = QRCodeGeneratorClass()
        qrGenerator.generateQRCodeToImageView(qrCode, UID)

        nameView.text = NAME
        idView.text = UID

        patientInformation(ROLE, UID)
        loadRoleButtons(ROLE, UID)
        loadScheduleCard(ROLE)

        val scheduleCard = findViewById<LinearLayout>(R.id.scheduleCardContent)
        scheduleCard.post {
            if (scheduleCard.height > 800) { // Hardcoded max height in pixels
                scheduleCard.layoutParams.height = 800
                scheduleCard.requestLayout()
            }
        }
        val maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, resources.displayMetrics).toInt()
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
            intent.putExtra("role", ROLE)
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

    private fun DoctorButtons(view: View, UID: String, ROLE: String) {
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
    private fun loadScheduleCard(role: String) {
        val scheduleContainer = findViewById<LinearLayout>(R.id.scheduleCardContent)
        val inflater = LayoutInflater.from(this)
        val USER = FirebaseAuth.getInstance().currentUser
        val UID = intent.getStringExtra("UID") ?: return
        val db = FirebaseFirestore.getInstance()

        val setScheduleLayout: (Int, Map<Int, String?>) -> Unit = { layoutRes, textMap ->
            val view = inflater.inflate(layoutRes, scheduleContainer, false)
            textMap.forEach { (id, value) ->
                view.findViewById<TextView?>(id)?.text = value ?: ""
            }
            scheduleContainer.removeAllViews()
            scheduleContainer.addView(view)
        }

        when (role) {
            // Patient Schedule Card
            "patient" -> {
                db.collection("appointments")
                    .whereEqualTo("patientId", UID)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d("FIRESTORE", "Query returned ${documents.size()} document(s)")

                        if (!documents.isEmpty) {
                            val doc = documents.first()
                            val status = doc.getString("status") ?: "none"
                            val date = doc.getString("date")
                            val today = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.ENGLISH).format(java.util.Date())

                            val layoutRes: Int
                            val textMap: Map<Int, String?>

                            Log.d("LOAD_CARD_DEBUG", "Loading appointment for UID: $UID")
                            Log.d("DATE_DEBUG", "today = $today, appointment date = $date")
                            Log.d("FIRESTORE", "Fetched doc with status: ${doc.getString("status")} and date: $date")

                            when {
                                status == "booked" && date != today -> {
                                    // Appointment booked
                                    layoutRes = R.layout.dashboard_schedule_patient_booked
                                    textMap = mapOf(
                                        R.id.textAppointmentDate to date,
                                        R.id.doctorName to doc.getString("doctorName")
                                    )
                                }

                                status == "queue_onboarding" && date == today -> {
                                    // Patient at Onboarding Desk
                                    layoutRes = R.layout.dashboard_schedule_patient_queue
                                    textMap = mapOf(
                                        R.id.textQueueLocation to "Onboarding Desk",
                                        R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                    )
                                }

                                status == "queue_doctor" && date == today -> {
                                    // Patient Queued to Doctor
                                    layoutRes = R.layout.dashboard_schedule_patient_queue
                                    textMap = mapOf(
                                        R.id.textQueueLocation to "ROOM 504",
                                        R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                    )
                                }

                                else -> {
                                    layoutRes = R.layout.dashboard_schedule_none
                                    textMap = emptyMap()
                                }
                            }

                            setScheduleLayout(layoutRes, textMap)
                        } else {
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                        }
                    }
                    .addOnFailureListener {
                        Log.e("FIRESTORE", "Failed to fetch patient appointment", it)
                        setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                    }
            }

            // Nurse Schedule Card
            "nurse" -> {
                db.collection("assignments").document(UID).get()
                    .addOnSuccessListener { doc ->
                        if (doc.getBoolean("hasTask") == true) {
                            val layoutRes = R.layout.dashboard_schedule_nurse
                            val textMap = mapOf(
                                R.id.patientName to doc.getString("patientName")
                            )
                            setScheduleLayout(layoutRes, textMap)
                        } else {
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                        }
                    }
                    .addOnFailureListener {
                        Log.e("FIRESTORE", "Failed to fetch nurse assignment", it)
                        setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                    }
            }

            // Doctor Schedule Card
            "doctor" -> {
                val today = java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.ENGLISH).format(java.util.Date())

                db.collection("appointments")
                    .whereEqualTo("doctorId", UID)
                    .whereEqualTo("status", "queue_doctor")
                    .whereEqualTo("readyToCall", true)
                    .whereEqualTo("date", today)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val doc = documents.first()
                            val layoutRes = R.layout.dashboard_schedule_doctor
                            val textMap = mapOf(
                                R.id.patientName to doc.getString("patientName")
                            )
                            setScheduleLayout(layoutRes, textMap)
                        } else {
                            setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
                        }
                    }
                    .addOnFailureListener {
                        Log.e("FIRESTORE", "Failed to fetch doctor queue", it)
                        setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
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