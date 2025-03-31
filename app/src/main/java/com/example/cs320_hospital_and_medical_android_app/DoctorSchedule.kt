package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class DoctorSchedule : AppCompatActivity() {

    //Initialize flipper
    private lateinit var viewFlipper: ViewFlipper

    //Initialize firebase
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_schedule)

        //Initialize firebase
        db = FirebaseFirestore.getInstance()

        //Access Flipper
        viewFlipper = findViewById(R.id.viewFlipper)

        //Get all doctors and display
        getAllDoctors()
    }

    data class Doctor(
        //Data structure for doctor
        val id: String = "",
        val PID: Long = 0L,
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
                    findViewById<TextView>(R.id.doctorID).text = doctor.PID.toString()
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
                    findViewById<TextView>(R.id.doctorID).text = doctor.PID.toString()
                    findViewById<TextView>(R.id.doctorName).text = "${doctor.firstName} ${doctor.lastName}"
                    findViewById<TextView>(R.id.doctorSpecialization).text = doctor.specialization
                }

                findViewById<Button>(R.id.btnSetSchedule).apply {
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

                    findViewById<ImageView>(R.id.deleteBtn).apply {
                        tag = document.id
                        setOnClickListener { deleteDoctorSchedule(DID, document.id) }
                    }
                }
                //Add card
                listSchedule?.addView(scheduleCard)
            }
        }
    }

    private fun addDoctorSchedule(id: String) {
        //Flip the view
        viewFlipper.displayedChild = 2
        //Access the input fields
        val dateInput: EditText = findViewById(R.id.dateInput)
        val timeInput: EditText = findViewById(R.id.timeInput)
        //Set the data structure
        findViewById<Button>(R.id.setBtn).setOnClickListener {
            val schedule = Schedule(
                date = dateInput.text.toString().trim(),
                day = "Saturday",
                time = timeInput.text.toString().trim()
            )
            //Access the schedule collection from Firebase
            val scheduleRef = db.collection("Doctors").document(id).collection("schedule").document()
            //Add the schedule details
            scheduleRef.set(schedule.copy(id = scheduleRef.id))
                .addOnSuccessListener { getOneDoctor(id) }
                .addOnFailureListener { e ->
                    createToast("FirestoreError: Failed to add schedule: ${e.message}")
                }
        }

        findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            getOneDoctor(id)
        }
    }

    private fun deleteDoctorSchedule(doctorId: String, scheduleId: String) {
        //Access the collection of schedule
        db.collection("Doctors").document(doctorId)
            .collection("schedule").document(scheduleId)
            //Delete
            .delete()
            .addOnSuccessListener { createToast("Schedule deleted successfully") }
            .addOnFailureListener { e -> createToast("Error deleting schedule: ${e.message}") }
    }

    private fun createToast(message: String) {
        //Create and display toast message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        //Flipping the view layouts
        if (viewFlipper.displayedChild > 0) {
            viewFlipper.showPrevious()
        } else {
            super.onBackPressed()
        }
    }
}