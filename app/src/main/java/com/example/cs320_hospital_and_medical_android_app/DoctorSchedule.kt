package com.example.cs320_hospital_and_medical_android_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
import java.util.Locale

class DoctorSchedule : AppCompatActivity() {

    //Initialize flipper
    private lateinit var viewFlipper: ViewFlipper

    //Initialize firebase
    private lateinit var db: FirebaseFirestore

    private var ROLE: String = ""
    private var UID: String = ""

    private var flipperNo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_schedule)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load User Info
        ROLE = intent.getStringExtra("ROLE") ?: return
        UID = intent.getStringExtra("UID") ?: "Unknown"

        //Initialize firebase
        db = FirebaseFirestore.getInstance()

        //Access Flipper
        viewFlipper = findViewById(R.id.viewFlipper)

        //Search doctors and display

        if (ROLE == "doctor") {
            flipperNo = 1
        }

       when(ROLE) {
           "patient" -> general()
           "nurse" -> general()
           "doctor" -> getOneDoctor(UID)
       }
    }

    private fun general() {
        getAllDoctors()
        searchDoctor()
    }

    data class Doctor(
        //Data structure for doctor
        val id: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val profilePicture: String = "",
        val specialization: String = ""
    )

    data class Schedule(
        //Data structure for schedule
        val id: String = "",
        val date: String = "",
        val day: String = "",
        val time: String = ""
    )

    private fun searchDoctor() {
        val searchBar: EditText = findViewById(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim().toLowerCase().replaceFirstChar { it.uppercase() }
                if (searchText.isNotEmpty()) {
                    getFilteredDoctors(searchText)

                } else {
                    getAllDoctors()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getFilteredDoctors(searchText: String) {
        val listDoctors: LinearLayout? = findViewById(R.id.listDoctor)
        val inflater = LayoutInflater.from(this)
        listDoctors?.removeAllViews()

        val doctorsCollection = db.collection("Doctors")
        val uniqueDoctors = HashSet<String>() // To avoid duplicates
        val doctorList = mutableListOf<Doctor>() // Store results before displaying

        // Search by Name (First/Last)
        doctorsCollection
            .orderBy("firstName")
            .startAt(searchText)
            .endAt(searchText + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val doctor = doc.toObject(Doctor::class.java).copy(id = doc.id)
                    if (uniqueDoctors.add(doctor.id)) doctorList.add(doctor)
                }
                updateDoctorList(doctorList, listDoctors, inflater)
            }

        // Search by Specialization
        doctorsCollection
            .orderBy("specialization")
            .startAt(searchText)
            .endAt(searchText + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val doctor = doc.toObject(Doctor::class.java).copy(id = doc.id)
                    if (uniqueDoctors.add(doctor.id)) doctorList.add(doctor)
                }
                updateDoctorList(doctorList, listDoctors, inflater)
            }
    }

    private fun getAllDoctors() {
        //Access the list
        val listDoctors: LinearLayout? = findViewById(R.id.listDoctor)
        //For inserting cards
        val inflater = LayoutInflater.from(this)
        //Access doctor list from Firebase
        db.collection("Doctors").addSnapshotListener { snapshot, _ ->
            listDoctors?.removeAllViews()
            //Display the data
            snapshot?.forEach { document ->
                val doctor = document.toObject(Doctor::class.java).copy(id = document.id)
                val doctorCard = inflater.inflate(R.layout.doctors_card, listDoctors, false).apply {
                    findViewById<TextView>(R.id.doctorID).text = doctor.id
                    findViewById<TextView>(R.id.doctorName).text = "${doctor.firstName} ${doctor.lastName}"
                    findViewById<TextView>(R.id.doctorSpecialization).text = doctor.specialization

                    findViewById<ImageView>(R.id.doctorSchedule).apply {
                        tag = doctor.id
                        setOnClickListener { getOneDoctor(doctor.id) }
                    }
                }
                //Add the card
                listDoctors?.addView(doctorCard)
            }
        }
    }

    private fun updateDoctorList(
        doctorList: List<Doctor>,
        listDoctors: LinearLayout?,
        inflater: LayoutInflater
    ) {
        listDoctors?.removeAllViews()
        for (doctor in doctorList) {
            val doctorCard = inflater.inflate(R.layout.doctors_card, listDoctors, false).apply {
                findViewById<TextView>(R.id.doctorID).text = doctor.id
                findViewById<TextView>(R.id.doctorName).text = "${doctor.firstName} ${doctor.lastName}"
                findViewById<TextView>(R.id.doctorSpecialization).text = doctor.specialization

                findViewById<ImageView>(R.id.doctorSchedule).apply {
                    tag = doctor.id
                    setOnClickListener { getOneDoctor(doctor.id) }
                }
            }
            listDoctors?.addView(doctorCard)
        }
    }


    private fun getOneDoctor(DID: String) {
        //Flip the layout
        viewFlipper.displayedChild = 1
        //Initialize the schedule of Doctors
        getDoctorSchedule(DID)
        //Access the doctors' information
        db.collection("Doctors").document(DID).addSnapshotListener { snapshot, _ ->
            snapshot?.toObject(Doctor::class.java)?.copy(id = snapshot.id)?.let { doctor ->
                //Display the doctors' information
                val profileSection: LinearLayout = findViewById(R.id.profileSection)
                with(profileSection) {
                    findViewById<TextView>(R.id.doctorID).text = doctor.id
                    findViewById<TextView>(R.id.doctorName).text = "${doctor.firstName} ${doctor.lastName}"
                    findViewById<TextView>(R.id.doctorSpecialization).text = doctor.specialization
                }

                val addSchedule: Button = findViewById(R.id.btnSetSchedule)

                if (ROLE == "patient") {
                    addSchedule.visibility = View.GONE
                }

                addSchedule.apply {
                    tag = doctor.id
                    setOnClickListener { addDoctorSchedule(DID) }
                }
            }
        }
    }

    private fun getDoctorSchedule(DID: String) {
        //Access the list
        val listSchedule: LinearLayout? = findViewById(R.id.listSchedule)
        //For inserting card
        val inflater = LayoutInflater.from(this)
        //Access the schedule from Firebase
        db.collection("Doctors").document(DID).collection("schedule").addSnapshotListener { snapshots, _ ->
            listSchedule?.removeAllViews()
            //Display the schedule info
            snapshots?.forEach { document ->
                val schedule = document.toObject(Schedule::class.java).copy(id = document.id)
                val scheduleCard = inflater.inflate(R.layout.schedule_card_clinic, listSchedule, false).apply {
                    findViewById<TextView>(R.id.scheduleDate).text = schedule.date
                    findViewById<TextView>(R.id.scheduleTime).text = "${schedule.day}: ${schedule.time}"

                    val deleteBtn: ImageView = findViewById<ImageView>(R.id.deleteBtn)

                    if (ROLE == "patient") {
                        deleteBtn.visibility = View.GONE
                    }

                    deleteBtn.apply {
                        tag = document.id
                        setOnClickListener { deleteDoctorSchedule(DID, document.id) }
                    }
                }
                //Add card
                listSchedule?.addView(scheduleCard)
            }
        }
    }

    private fun addDoctorSchedule(DID: String) {
        //Flip the view
        viewFlipper.displayedChild = 2
        //Initialize Calendar & Time
        initializeCalendar()
        initializeTime()
        //Access the input fields
        val dateInput: EditText = findViewById(R.id.dateInput)
        dateInput.text = null
        val timeInput: EditText = findViewById(R.id.timeInput)
        timeInput.text = null

        findViewById<Button>(R.id.setBtn).setOnClickListener {
            if (dateInput.text.isNotEmpty() && timeInput.text.isNotEmpty()){
                //Get the day
                val inputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) // Matches "March 5, 2025"
                val date = inputFormat.parse(dateInput.text.toString().trim())
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

                val schedule = Schedule(
                    date = dateInput.text.toString().trim(),
                    day = dayFormat.format(date).toString(),
                    time = timeInput.text.toString().trim()
                )
                //Access the schedule collection from Firebase
                val scheduleRef = db.collection("Doctors").document(DID).collection("schedule").document()
                //Add the schedule details
                scheduleRef.set(schedule.copy(id = scheduleRef.id))
                    .addOnSuccessListener { getOneDoctor(DID) }
                    .addOnFailureListener { e ->
                        createToast("Failed to add schedule")
                    }
            } else {
                createToast("Input all fields")
            }
        }

        findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            getOneDoctor(DID)
        }
    }

    private fun deleteDoctorSchedule(DID: String, SCHEDID: String) {
        //Access the collection of schedule
        db.collection("Doctors").document(DID)
            .collection("schedule").document(SCHEDID)
            //Delete
            .delete()
            .addOnSuccessListener { createToast("Schedule deleted successfully") }
            .addOnFailureListener { createToast("Failed to delete schedule") }
    }

    private fun createToast(message: String) {
        //Create and display toast message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun initializeTime() {
        //Time Input
        findViewById<EditText>(R.id.timeInput).setOnClickListener(){
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)

            // Select FROM time
            TimePickerDialog(this, { _, fromHour, fromMinute ->
                val fromTime = formatTime12Hour(fromHour, fromMinute)

                // Select TO time
                TimePickerDialog(this, { _, toHour, toMinute ->
                    val toTime = formatTime12Hour(toHour, toMinute)

                    // Set the selected time range in EditText
                    findViewById<EditText>(R.id.timeInput).setText("$fromTime - $toTime")
                }, hour, minute, false).show()  // "false" for 12-hour format

            }, hour, minute, false).show()  // "false" for 12-hour format
        }
    }

    private fun formatTime12Hour(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val formattedHour = if (hour % 12 == 0) 12 else hour % 12
        return String.format("%02d:%02d %s", formattedHour, minute, amPm)
    }

    private fun initializeCalendar() {
        //Calendar Input
        findViewById<EditText>(R.id.dateInput).setOnClickListener(){
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
                    findViewById<EditText>(R.id.dateInput).setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePicker.show()
        }
    }

    override fun onBackPressed() {
        //Flipping the view layouts
        if (viewFlipper.displayedChild > flipperNo) {
            viewFlipper.showPrevious()
        } else {
            super.onBackPressed()
        }
    }
}