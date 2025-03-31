package com.example.cs320_hospital_and_medical_android_app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject

class DoctorSchedule : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper

    //Firebase Initialization
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        //Firebase Initialization
        db = FirebaseFirestore.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_schedule)

        viewFlipper = findViewById(R.id.viewFlipper)

        getAllDoctors()

    }

    data class Doctor (
        val id: String = "",
        val PID: Long = 0L,
        val firstName: String = "",
        val lastName: String = "",
        val profilePicture: String = "",
        val specialization: String = ""
    )

    data class Schedule (
        val id: String = "",
        val date: String = "",
        val day: String = "",
        val time: String = "",
    )

    private fun getAllDoctors() {
        //Inflater initialization
        val listDoctors: LinearLayout? = findViewById(R.id.listDoctor)
        val inflater = LayoutInflater.from(this)

        db.collection("Doctors")
            .addSnapshotListener  { snapshots, error ->

                listDoctors?.removeAllViews()

                snapshots?.let {
                    for (document in it) {
                        val doctor = document.toObject(Doctor::class.java).copy(
                            id = document.id
                        )

                        //Inflating instances of doctor
                        val doctorCard = inflater.inflate(R.layout.doctors_card, listDoctors, false)

                        val doctorID: TextView = doctorCard.findViewById(R.id.doctorID)
                        val doctorName: TextView = doctorCard.findViewById(R.id.doctorName)
                        val doctorSpecialization: TextView = doctorCard.findViewById(R.id.doctorSpecialization)

                        doctorID.text = doctor.PID.toString()
                        doctorName.text = "${doctor.firstName} ${doctor.lastName}"
                        doctorSpecialization.text = doctor.specialization

                        listDoctors?.let {
                            it.addView(doctorCard)
                        }

                        val doctorSchedule: ImageView = doctorCard.findViewById(R.id.doctorSchedule)
                        doctorSchedule.setOnClickListener() {
                            getOneDoctor(doctor.id)
                        }
                    }
                }
            }
    }

    private fun getOneDoctor(id: String) {
        viewFlipper.displayedChild = 1

        db.collection("Doctors")
            .document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val doctor = snapshot?.toObject(Doctor::class.java)?.copy(id = snapshot.id)

                val profileSection: LinearLayout = findViewById(R.id.profileSection)

                val doctorID: TextView = profileSection.findViewById(R.id.doctorID)
                val doctorName: TextView = profileSection.findViewById(R.id.doctorName)
                val doctorSpecialization: TextView = profileSection.findViewById(R.id.doctorSpecialization)

                doctorID.text = doctor?.PID.toString()
                doctorName.text = "${doctor?.firstName} ${doctor?.lastName}"
                doctorSpecialization.text = doctor?.specialization

                val btnSetSchedule: Button = findViewById(R.id.btnSetSchedule)
                btnSetSchedule.setOnClickListener() {
                    addDoctorSchedule(id)
                }

                val deleteBtn: ImageView = findViewById(R.id.deleteBtn)
                deleteBtn.setOnClickListener(){
                    createToast("Deleted")
                }
            }

        getDoctorSchedule(id)
    }

    private fun getDoctorSchedule(id: String) {
        //Inflater initialization
        val listSchedule: LinearLayout? = findViewById(R.id.listSchedule)
        val inflater = LayoutInflater.from(this)

        db.collection("Doctors").document(id).collection("schedule")
            .addSnapshotListener  { snapshots, error ->

                listSchedule?.removeAllViews()

                snapshots?.let {
                    for (document in it) {
                        val schedule = document.toObject(Schedule::class.java).copy(
                            id = document.id
                        )

                        //Inflating instances of doctor
                        val scheduleCard = inflater.inflate(R.layout.schedule_card_clinic, listSchedule, false)

                        val scheduleDate: TextView = scheduleCard.findViewById(R.id.scheduleDate)
                        val scheduleTime: TextView = scheduleCard.findViewById(R.id.scheduleTime)

                        scheduleDate.text = schedule.date
                        scheduleTime.text = "${schedule.day}: ${schedule.time}"

                        listSchedule?.let {
                            it.addView(scheduleCard)
                        }

                    }
                }
            }
    }

    private fun addDoctorSchedule(id: String){
        viewFlipper.displayedChild = 2

        val dateInput: EditText = findViewById(R.id.dateInput)
        val timeInput: EditText = findViewById(R.id.timeInput)

        val setBtn: Button = findViewById(R.id.setBtn)
        setBtn.setOnClickListener() {
            val schedule = Schedule (
                date = dateInput.text.toString().trim(),
                day = "Saturday",
                time = timeInput.text.toString().trim()
            )

            val scheduleRef = db.collection("Doctors").document(id).collection("schedule").document()
            val scheduleWithId = schedule.copy(id = scheduleRef.id)

            scheduleRef.set(scheduleWithId)
                .addOnSuccessListener {
                    getOneDoctor(id)
                }
                .addOnSuccessListener { e ->
                    if (e != null){
                        createToast("FirestoreError: Failed to add schedule: $e")
                    }
                }
        }

        val cancelBtn: Button = findViewById(R.id.cancelBtn)
        cancelBtn.setOnClickListener() {
            getOneDoctor(id)
        }
    }

    private fun createToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (viewFlipper.displayedChild > 0){
            viewFlipper.showPrevious()
        } else {
            super.onBackPressed()
        }
    }
}