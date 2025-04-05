package com.example.cs320_hospital_and_medical_android_app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.snap
import com.example.cs320_hospital_and_medical_android_app.DoctorSchedule.Schedule
import com.google.firebase.firestore.FirebaseFirestore
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.time.ZonedDateTime

class PatientAppointment : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper

    //Initialize firebase
    private lateinit var db: FirebaseFirestore

    private val doctorList = mutableListOf<Doctor>()
    private val dateTimeList = mutableListOf<Schedule>()
    private var selectedDoctorId: String? = null
    private var patientName: String? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appointments)

        //Initialize firebase
        db = FirebaseFirestore.getInstance()

        viewFlipper = findViewById(R.id.viewFlipper)

        val PID = intent.getStringExtra("UID") ?: "Unknown"

        getAppointment(PID)

        val addAppointmentBtn: ImageButton = findViewById(R.id.addAppointmentBtn)
        addAppointmentBtn.setOnClickListener(){
            addAppointment(PID)
        }

    }

    private fun getAppointment(PID: String) {
        //Access the list
        val listAppointments: LinearLayout? = findViewById(R.id.listAppointments)
        //For inserting card
        val inflater = LayoutInflater.from(this)
        //Access the schedule from Firebase
        db.collection("appointments")
            .whereEqualTo("patientID", PID).addSnapshotListener { snapshots, _ ->
            listAppointments?.removeAllViews()
            //Display the schedule info
            snapshots?.forEach { document ->
                val schedule = document.toObject(Appointment::class.java)
                val appointmentCard = inflater.inflate(R.layout.schedule_appointment_card_patient, listAppointments, false).apply {
                    findViewById<TextView>(R.id.doctorName).text = "Dr. ${schedule.doctorName}"
                    findViewById<TextView>(R.id.scheduleDateTime).text = "${schedule.date} at ${schedule.time}"
                    findViewById<TextView>(R.id.reasonOut).text = "Reason: ${schedule.reason}"

                    val deleteBtn: ImageView = findViewById(R.id.deleteBtn)

                    deleteBtn.apply {
                        tag = document.id
                        setOnClickListener { deleteAppointment(document.id) }
                    }

                }
                //Add card
                listAppointments?.addView(appointmentCard)
            }
        }
    }

    private fun deleteAppointment(appointmentId: String) {
        //Access the collection of schedule
        db.collection("appointments").document(appointmentId)
            .delete()
            .addOnSuccessListener { createToast("Appointment deleted successfully") }
            .addOnFailureListener { createToast("Failed to delete appointment") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addAppointment(PID: String) {
        viewFlipper.displayedChild = 1

        val doctorInput: AutoCompleteTextView = findViewById(R.id.doctorInput)
        val dateTimeInput: AutoCompleteTextView = findViewById(R.id.dateTimeInput)
        val reasonInput: EditText = findViewById(R.id.reasonInput)

        // Fetch doctors and populate the doctor dropdown
        db.collection("Doctors").addSnapshotListener { snapshot, _ ->
            snapshot?.forEach { document ->
                val fullName = document.getString("firstName") + " " + document.getString("lastName")
                val id = document.id
                val doctor = Doctor(id, fullName)
                doctorList.add(doctor)
            }

            val doctorNames = doctorList.map { it.fullName }
            dropdown(doctorInput, doctorNames) { selectedDoctorName ->
                val selectedDoctor = doctorList.find { it.fullName == selectedDoctorName }
                selectedDoctor?.let {
                    selectedDoctorId = it.id
                    loadDoctorSchedule(it.id, dateTimeInput) // Load schedule for the selected doctor
                }
            }
        }

        db.collection("Patients").document(PID)
            .addSnapshotListener {snapshot, _ ->
                if (snapshot != null) {
                    patientName = "${snapshot.getString("firstName")} ${snapshot.getString("lastName")}"
                }
            }

        // Book appointment logic
        val bookBtn: Button = findViewById(R.id.bookBtn)
        bookBtn.setOnClickListener {
            val selectedDoctorName = doctorInput.text.toString().trim()
            val selectedReason = reasonInput.text.toString().trim()

            // Retrieve (date, time) pair from tag
            val selectedDateTimePair = dateTimeInput.getTag(R.id.dateTimeInput) as? Pair<String, String>
            val selectedDate = selectedDateTimePair?.first ?: "Unknown Date"
            val selectedTime = selectedDateTimePair?.second ?: "Unknown Time"

            // Create the appointment object
            val appointment = hashMapOf(
                "patientID" to PID,
                "patientName" to patientName,
                "doctorID" to selectedDoctorId,
                "doctorName" to selectedDoctorName,
                "createdAt" to ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME),
                "date" to selectedDate,
                "time" to selectedTime,
                "reason" to selectedReason,
                "queueLocation" to "TBD",
                "queueNumber" to 0,
                "readyToCall" to false,
                "status" to "booked"
            )

            if (!selectedDoctorName.isEmpty() && !selectedReason.isEmpty() && !selectedDateTimePair.toString().isEmpty()){
                // Push appointment to Firebase
                db.collection("appointments").document()
                    .set(appointment)
                    .addOnSuccessListener {
                        createToast("Scheduled Successfully!")
                    }
                    .addOnFailureListener {
                        createToast("Failed to Schedule Appointment.")
                    }

                listAppointment()
            } else {
                createToast("Input all fields")
            }

        }

        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener {
            listAppointment()
        }
    }

    private fun loadDoctorSchedule(doctorId: String, dateTimeInput: AutoCompleteTextView) {
        db.collection("Doctors").document(doctorId).collection("schedule")
            .addSnapshotListener { snapshot, _ ->
                val scheduleList = mutableListOf<String>()
                val scheduleMap = mutableMapOf<String, Pair<String, String>>() // (ID -> (date, time))

                snapshot?.forEach { document ->
                    val date = document.getString("date") ?: "Unknown Date"
                    val time = document.getString("time") ?: "Unknown Time"
                    scheduleMap[document.id] = Pair(date, time)
                    scheduleList.add("$date at $time")
                }

                // Store the map inside EditText tag for later use
                dateTimeInput.setTag(R.id.dateTimeInput, scheduleMap)

                // Populate the schedule dropdown
                dropdown(dateTimeInput, scheduleList) { selectedScheduleText ->
                    Log.d("DEBUG", "Selected Schedule: $selectedScheduleText")

                    val selectedEntry = scheduleMap.entries.find {
                        "${it.value.first} at ${it.value.second}" == selectedScheduleText
                    }

                    selectedEntry?.let {
                        dateTimeInput.setTag(R.id.dateTimeInput, it.value)
                        Log.d("DEBUG", "Stored in tag: Date=${it.value.first}, Time=${it.value.second}")
                    } ?: Log.e("ERROR", "Selected schedule not found in map")
                }
            }
    }

    // Dropdown helper function for populating AutoCompleteTextView
    private fun dropdown(input: AutoCompleteTextView, list: List<String>, onItemSelected: (String) -> Unit) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, list)
        input.setAdapter(adapter)
        input.setOnClickListener {
            input.showDropDown()
        }
        input.threshold = 1

        input.setOnItemClickListener { parent, _, position, _ ->
            val selectedText = parent.getItemAtPosition(position).toString()
            onItemSelected(selectedText)  // Let the caller handle it
        }
    }

    private fun listAppointment() {
        viewFlipper.displayedChild = 0
    }

    private fun createToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (viewFlipper.displayedChild > 0){
            listAppointment()
        } else {
            super.onBackPressed()
        }
    }

    data class Doctor(val id: String = "", val fullName: String = "")
    data class Schedule(val id: String = "", val dateTime: String = "")
    data class Appointment(
        val createdAt: String = "",
        val date: String = "",
        val time: String = "",
        val doctorName: String = "",
        val patientID: String = "",
        val reason: String = "",
        val queueLocation: String = "",
        val queueNumber: Int = 0,
        val readyToCall: Boolean = false,
        val status: String = "",
    )

}