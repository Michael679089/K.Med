package com.example.cs320_hospital_and_medical_android_app

import android.content.Context
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

    fun updateAppointment(DID: String, PID: String){
        getAppointmentsByPatientID(PID) { _, documentIds ->
            if (documentIds.isNotEmpty()) {
                for (docId in documentIds) {
                    db.collection("appointments")
                        .document(docId)
                        .addSnapshotListener { doc, exception ->
                            if (doc != null && doc.exists()) {
                                db.collection("appointments")
                                    .document(doc.id)
                                    .update(
                                        mapOf(
                                            "status" to "done",
                                            "queueNumber" to 0,
                                            "queueStation" to "For Exit",
                                        )
                                    )
                            }
                        }
                }
            }
        }
    }

    fun updateNurseTask(NID: String, PID: String, BP_VAL: Double, WEIGHT: Double) {
        getAppointmentsByPatientID(PID) { _, documentIds ->
            if (documentIds.isNotEmpty()) {
                for (docId in documentIds) {
                    db.collection("appointments")
                        .document(docId)
                        .addSnapshotListener { doc, exception ->
                            if (doc != null && doc.exists()) {
                                db.collection("assignments")
                                    .whereEqualTo("nurseId", NID)
                                    .whereEqualTo("appointmentId", doc.id)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        if (!snapshot.isEmpty) {
                                            for (document in snapshot) {
                                                document.reference.delete()
                                            }
                                        }
                                    }

                                val DID = doc.getString("doctorID").toString()

                                db.collection("Doctors")
                                    .document(DID)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val DOCTOR_ROOM = snapshot.getString("room").toString()
                                        db.collection("appointments")
                                            .document(doc.id)
                                            .update(
                                                mapOf(
                                                    "status" to "queue_doctor",
                                                    "queueNumber" to 1,
                                                    "queueStation" to "ROOM $DOCTOR_ROOM",
                                                    "bloodPressure" to BP_VAL,
                                                    "weight" to WEIGHT,
                                                )
                                            )
                                    }
                            }
                        }
                }
            }
        }
    }

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

    // authentication of patient users
    fun authenticateUser(
        email: String,
        password: String,
        callback: (Map<String, String>?) -> Unit
    )
    {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val firebaseUid = auth.currentUser?.uid

                if (firebaseUid != null) {
                    db.collection("users").document(firebaseUid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc != null && userDoc.exists()) {
                                val role = userDoc.getString("role")
                                val uid = userDoc.getString("accountId")

                                if (role != null && uid != null) {
                                    if (role == "patient-notreg") {
                                        callback(mapOf("status" to "patient-notreg", "role" to role))
                                    } else {
                                        val collectionName = role.replaceFirstChar { it.uppercase() } + "s"
                                        db.collection(collectionName).document(uid)
                                            .get()
                                            .addOnSuccessListener { profileDoc ->
                                                val firstName = profileDoc.getString("firstName") ?: ""
                                                val lastName = profileDoc.getString("lastName") ?: ""
                                                val name = "$firstName $lastName".trim()

                                                callback(
                                                    mapOf(
                                                        "status" to "success",
                                                        "role" to role,
                                                        "uid" to uid,
                                                        "name" to name
                                                    )
                                                )
                                            }
                                    }
                                } else {
                                    Log.e("DEBUG", "ERROR: HANDLER -> Invalid user profile data")
                                    callback(null)
                                }
                            } else {
                                Log.e("DEBUG", "ERROR: DBHANDLER -> User profile not found")
                                callback(null)
                            }
                        }
                } else {
                    Log.e("DEBUG", "ERROR: DBHANDLER -> Authentication Failed")
                    callback(null)
                }
            }
            .addOnFailureListener {
                Log.e("DEBUG", "ERROR: DBHANDLER -> Authentication Failed")
                callback(null)
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

    fun getNameOfLoggedInUser(userID: String, userRole: String, callback: (String) -> Unit) {
        val collectionName = when (userRole.lowercase()) {
            "patient" -> "Patients"
            "nurse" -> "Nurses"
            "doctor" -> "Doctors"
            else -> {
                Log.e("DEBUG", "Invalid user role: $userRole")
                callback("")
                return
            }
        }

        val dbTable = db.collection(collectionName)
        dbTable.document(userID).get() // Access the document with ID = userID
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Extract firstName and lastName from the document
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""

                    // Combine firstName and lastName to form the full name
                    val fullName = "$firstName $lastName".trim()

                    Log.d("DEBUG", "User Full Name: $fullName")
                    callback(fullName) // Pass the full name to the callback
                } else {
                    Log.e("DEBUG", "No document found with ID: $userID")
                    callback("") // No document found
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Failed to fetch user data", e)
                callback("")
            }
    }

    fun getRoleOfLoggedInUser(accountId: String, callback: (String) -> Unit) {
        val dbTable = db.collection("users")

        // Query directly by accountId instead of userID
        dbTable.whereEqualTo("accountId", accountId).limit(1).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents.first()
                    val userRole = document.getString("role")?.lowercase() ?: ""

                    Log.d("DEBUG", "Fetched user role: $userRole for accountId: $accountId")
                    callback(userRole)
                } else {
                    Log.e("DEBUG", "No user found with accountId: $accountId")
                    callback("")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Failed to fetch user role", e)
                callback("")
            }
    }
}
