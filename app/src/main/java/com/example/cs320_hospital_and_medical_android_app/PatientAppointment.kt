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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appointments)

        //Initialize firebase
        db = FirebaseFirestore.getInstance()

        viewFlipper = findViewById(R.id.viewFlipper)

        val PID = intent.getStringExtra("PID") ?: "Unknown"

        getAppointment(PID)

        val addAppointmentBtn: ImageButton = findViewById(R.id.addAppointmentBtn)
        addAppointmentBtn.setOnClickListener(){
            appointmentForm(PID)
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
                    findViewById<TextView>(R.id.scheduleDateTime).text = schedule.schedule
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
    private fun appointmentForm(PID: String){
        viewFlipper.displayedChild = 1

        val doctorInput: AutoCompleteTextView = findViewById(R.id.doctorInput)
        val dateTimeInput: AutoCompleteTextView = findViewById(R.id.dateTimeInput)
        val reasonInput: EditText = findViewById(R.id.reasonInput)

        db.collection("Doctors").addSnapshotListener{
                snapshot, _ ->
            snapshot?.forEach {
                    document ->
                val fullName = document.getString("firstName") + " " + document.getString("lastName")
                val id = document.id
                val doctor = Doctor(id, fullName)
                doctorList.add(doctor)
            }

            val doctorNames = doctorList.map { it.fullName }
            dropdown(doctorInput, doctorNames) {
                selectedDoctorId -> loadDoctorSchedule(selectedDoctorId, dateTimeInput)
            }
        }

        val bookBtn: Button = findViewById(R.id.bookBtn)
        bookBtn.setOnClickListener() {

            val appointment = Appointment(
                createdAt = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME),
                doctorName = doctorInput.text.toString().trim(),
                schedule = dateTimeInput.text.toString().trim(),
                patientID = PID,
                reason = reasonInput.text.toString().trim(),
                queueLocation = "TBD",
                queueNumber = 0,
                readyToCall = false,
                status = "Booked",
            )

            db.collection("appointments").document()
                .set(appointment)

            createToast("Scheduled!")
            listAppointment()
        }

        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener() {
            listAppointment()
        }
    }

    data class Doctor(val id: String = "", val fullName: String = "")
    data class Schedule(val id: String = "", val dateTime: String = "")
    data class Appointment(
        val createdAt: String = "",
        val schedule: String = "",
        val doctorName: String = "",
        val patientID: String = "",
        val reason: String = "",
        val queueLocation: String = "",
        val queueNumber: Int = 0,
        val readyToCall: Boolean = false,
        val status: String = "",
    )

    private fun dropdown(input: AutoCompleteTextView, list: List<String>, onItemSelected: (String) -> Unit) {
        val items = list
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        input.setAdapter(adapter)
        input.setOnClickListener() {
            input.showDropDown()
        }
        input.threshold = 1

        input.setOnItemClickListener { parent, _, position, _ ->
            val selectedDoctorName = parent.getItemAtPosition(position).toString()

            // Find doctor by name and get the ID
            val selectedDoctor = doctorList.find { it.fullName == selectedDoctorName }
            if (selectedDoctor != null) {
                onItemSelected(selectedDoctor.id!!)
            }
        }
    }

    private fun loadDoctorSchedule(doctorId: String, dateTimeInput: AutoCompleteTextView) {
        db.collection("Doctors").document(doctorId).collection("schedule").addSnapshotListener{
                snapshot, _ ->
            snapshot?.forEach {
                    document ->
                val dateTime = document.getString("date") + ": " + document.getString("time")
                val id = document.id
                val schedule = Schedule(id, dateTime)
                dateTimeList.add(schedule)
            }

            val dateTime = dateTimeList.map { it.dateTime }
            dropdown(dateTimeInput, dateTime) { }
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

}