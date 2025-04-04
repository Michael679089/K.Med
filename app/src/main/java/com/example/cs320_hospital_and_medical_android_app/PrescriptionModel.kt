package com.example.cs320_hospital_and_medical_android_app.models

data class PrescriptionModel(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val date: String = "",
    val details: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)
