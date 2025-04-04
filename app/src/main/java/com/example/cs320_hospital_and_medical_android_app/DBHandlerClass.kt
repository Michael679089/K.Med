package com.example.cs320_hospital_and_medical_android_app

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DBHandlerClass() {
    // Firebase Initialization
    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()



    // # FUNCTIONS

    // ## 🔹 Unified function to get a patient's appointments
    private fun getAppointmentsByPatientID(
        patientIDValue: String,
        callback: (List<Map<String, Any>>, List<String>) -> Unit
    ) {
        db.collection("appointments")
            .whereEqualTo("patientID", patientIDValue)
            .get()
            .addOnSuccessListener { documents ->
                val appointments = mutableListOf<Map<String, Any>>()
                val documentIds = mutableListOf<String>()

                if (!documents.isEmpty) {
                    for (document in documents) {
                        appointments.add(document.data)
                        documentIds.add(document.id)
                    }
                }

                callback(appointments, documentIds)
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "ERROR getting appointments: ", e)
                callback(emptyList(), emptyList())
            }
    }

    // ## Get the latest appointment based on date
    fun getLatestAppointment(editTextPIDValue: String, callback: (Map<String, Any>?) -> Unit) {
        getAppointmentsByPatientID(editTextPIDValue) { appointments, _ ->
            if (appointments.isNotEmpty()) {
                val latestAppointment = appointments.maxByOrNull { appointment ->
                    val dateString = appointment["date"] as? String
                    val parsedDate = try {
                        SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).parse(dateString ?: "")
                    } catch (e: Exception) {
                        Log.e("ERROR", "Error parsing date: $dateString", e)
                        null
                    }
                    parsedDate ?: Date(0) // Default to a very old date
                }

                Log.d("DEBUG", "Latest Appointment: $latestAppointment")
                callback(latestAppointment)
            } else {
                callback(null)
            }
        }
    }

    // ## Update `readyToCall` field for a patient's latest appointment
    fun setReadyToCall(setReadyToCallBoolean: Boolean, patientIDValue: String) {
        getAppointmentsByPatientID(patientIDValue) { _, documentIds ->
            if (documentIds.isNotEmpty()) {
                for (docId in documentIds) {
                    db.collection("appointments")
                        .document(docId)
                        .update("readyToCall", setReadyToCallBoolean)
                        .addOnSuccessListener {
                            Log.d("DEBUG", "Successfully updated readyToCall to $setReadyToCallBoolean")
                        }
                        .addOnFailureListener { e ->
                            Log.e("DEBUG", "Failed to update readyToCall", e)
                        }
                }
            } else {
                Log.e("DEBUG", "No appointment document found to update readyToCall")
            }
        }
    }
}
