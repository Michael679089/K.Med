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
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import android.view.View
import android.widget.LinearLayout
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)


        // Load User Info
        val role = intent.getStringExtra("role") ?: return
        val name = intent.getStringExtra("name") ?: "No Name"
        Log.d("DASHBOARD_INTENT", "Received name: $name")
        val customUid = intent.getStringExtra("uid") ?: "Unknown"

        val nameView = findViewById<TextView>(R.id.accountName)
        val idView = findViewById<TextView>(R.id.accountID)
        nameView.text = name
        idView.text = customUid

        loadRoleButtons(role)
        loadScheduleCard(role)

    }

    // Load RBA Buttons
    private fun loadRoleButtons(role: String) {
        val container = findViewById<FrameLayout>(R.id.buttonSectionContainer)
        val inflater = LayoutInflater.from(this)

        val layoutRes = when (role) {
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
            when (role) {
                "patient" -> PatientButtons(view)
                "doctor" -> DoctorButtons(view)
                "nurse"  -> NurseButtons(view)
            }
        }
    }

    // Button Listeners
    private fun PatientButtons(view: View) {
        val doctorBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.scheduleBtn)
        val prescriptionBtn = view.findViewById<LinearLayout>(R.id.prescriptionBtn)

        doctorBtn.setOnClickListener {
            startActivity(Intent(this, DoctorSchedule::class.java))
        }

        scheduleBtn.setOnClickListener {
            startActivity(Intent(this, PatientAppointment::class.java))
        }

        prescriptionBtn.setOnClickListener {
            startActivity(Intent(this, Prescription::class.java))
        }
    }
    private fun DoctorButtons(view: View) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val scheduleBtn = view.findViewById<LinearLayout>(R.id.doctorAccessSchedule)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("role", "doctor")
            startActivity(intent)
        }

        scheduleBtn.setOnClickListener {
            startActivity(Intent(this, DoctorSchedule::class.java))
        }
    }
    private fun NurseButtons(view: View) {
        val qrBtn = view.findViewById<LinearLayout>(R.id.patientQRBtn)
        val doctorBtn = view.findViewById<LinearLayout>(R.id.doctorBtn)

        qrBtn.setOnClickListener {
            val intent = Intent(this, QRReader::class.java)
            intent.putExtra("role", "nurse")
            startActivity(intent)
        }

        doctorBtn.setOnClickListener {
            startActivity(Intent(this, DoctorSchedule::class.java))
        }
    }

    // Load Schedule Card
    private fun loadScheduleCard(role: String) {
        val scheduleContainer = findViewById<FrameLayout>(R.id.scheduleCardContent)
        val inflater = LayoutInflater.from(this)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = intent.getStringExtra("uid") ?: return
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
                    .whereEqualTo("patientId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val doc = documents.first()
                            val status = doc.getString("status") ?: "none"
                            val date = doc.getString("date")
                            val today = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(Date())

                            val layoutRes: Int
                            val textMap: Map<Int, String?>

                            when {
                                status == "booked" && date != today -> {
                                    layoutRes = R.layout.dashboard_schedule_patient_booked
                                    textMap = mapOf(
                                        R.id.textAppointmentDate to date,
                                        R.id.doctorName to doc.getString("doctorName")
                                    )

                                    // Inflate the booked card manually
                                    val view = inflater.inflate(layoutRes, scheduleContainer, false)
                                    view.findViewById<TextView>(R.id.textAppointmentDate)?.text = date
                                    view.findViewById<TextView>(R.id.doctorName)?.text = doc.getString("doctorName")

                                    val confirmBtn = view.findViewById<Button>(R.id.confirmArrivalBtn)
                                    confirmBtn.setOnClickListener {
                                        val appointmentId = doc.id
                                        val patientName = doc.getString("patientName") ?: "Unknown"
                                        val nurseUid = "NID00000001"

                                        db.collection("appointments").document(appointmentId)
                                            .update(
                                                mapOf(
                                                    "status" to "queue_onboarding",
                                                    "queueNumber" to 1,
                                                    "queueType" to "onboarding",
                                                    "readyToCall" to false
                                                )
                                            )

                                        db.collection("assignments").document(nurseUid)
                                            .set(
                                                mapOf(
                                                    "hasTask" to true,
                                                    "patientName" to patientName,
                                                    "appointmentId" to appointmentId
                                                )
                                            )

                                        Toast.makeText(this, "Arrival confirmed!", Toast.LENGTH_SHORT).show()
                                        loadScheduleCard("patient")
                                    }

                                    scheduleContainer.removeAllViews()
                                    scheduleContainer.addView(view)
                                    return@addOnSuccessListener
                                }

                                status == "queue_onboarding" && date == today -> {
                                    layoutRes = R.layout.dashboard_schedule_patient_queue
                                    textMap = mapOf(
                                        R.id.textQueueLocation to "Onboarding Desk",
                                        R.id.textQueueNumber to doc.get("queueNumber")?.toString()
                                    )
                                }

                                status == "queue_doctor" && date == today -> {
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
                db.collection("assignments").document(userId).get()
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
                    .whereEqualTo("doctorId", userId)
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
                Log.d("FIRESTORE", "No appointment document found for $userId")
                setScheduleLayout(R.layout.dashboard_schedule_none, emptyMap())
            }
        }
    }
}
