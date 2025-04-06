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


    fun getDB(): FirebaseFirestore {
        return db
    }

    fun getAuth() : FirebaseAuth {
        return auth
    }

    fun addDoctor(
        email: String, // Email input for doctor
        password: String, // Password input for doctor
        firstName: String,
        lastName: String,
        roomNum: String,
        specialization: String,
        callback: (Boolean) -> Unit
    ) {
        // Step 1: Create the Firebase Authentication user (Doctor)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid

                if (userId != null) {
                    // Step 2: Add user to the Firestore "users" collection
                    val userMap = hashMapOf(
                        "role" to "doctor",  // Role is doctor
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "roomNum" to roomNum,
                        "specialization" to specialization,
                        "accountId" to ""  // Initially set accountId as blank
                    )

                    // Store user details in Firestore
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            // Step 3: Add doctor profile to "Doctors" collection
                            val doctorMap = hashMapOf(
                                "firebaseUid" to userId,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "roomNum" to roomNum,
                                "specialization" to specialization,
                                "profilePicture" to ""  // Blank profile picture initially
                            )

                            val doctorId = "DID" + System.currentTimeMillis() // Generate a unique doctor ID

                            FirebaseFirestore.getInstance().collection("Doctors").document(doctorId)
                                .set(doctorMap)
                                .addOnSuccessListener {
                                    // Step 4: Update the user's accountId in the "users" collection
                                    FirebaseFirestore.getInstance().collection("users").document(userId)
                                        .update("accountId", doctorId)
                                        .addOnSuccessListener {
                                            Log.d("DEBUG", "User accountId updated with DoctorID")

                                            // Step 5: Return success via callback
                                            callback(true)  // Return success
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DEBUG", "Error updating accountId: ", e)
                                            callback(false)  // Return failure
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DEBUG", "Error adding doctor to Doctors collection: ", e)
                                    callback(false)  // Return failure
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DEBUG", "Error adding doctor to Users collection: ", e)
                            callback(false)  // Return failure
                        }
                } else {
                    Log.e("DEBUG", "Error: User creation failed")
                    callback(false)  // Return failure if the user creation failed
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Error creating user with email and password: ", e)
                callback(false)  // Return failure if the user creation failed
            }
    }


    fun addNurse(
        email: String, // Email input for nurse
        password: String, // Password input for nurse
        firstName: String,
        lastName: String,
        callback: (Boolean) -> Unit
    ) {
        // Step 1: Create the Firebase Authentication user
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid

                if (userId != null) {
                    // Step 2: Add user to the Firestore "users" collection
                    val userMap = hashMapOf(
                        "role" to "nurse",  // Role is nurse
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "accountId" to ""  // Initially set accountId as blank
                    )

                    // Store user details in Firestore
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            // Step 3: Add nurse profile to "Nurses" collection
                            val nurseMap = hashMapOf(
                                "firebaseUid" to userId,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "profilePicture" to ""  // Blank profile picture initially
                            )

                            val nurseId = "NID" + System.currentTimeMillis() // Generate a unique nurse ID

                            FirebaseFirestore.getInstance().collection("Nurses").document(nurseId)
                                .set(nurseMap)
                                .addOnSuccessListener {
                                    // Step 4: Update the user's accountId in the "users" collection
                                    FirebaseFirestore.getInstance().collection("users").document(userId)
                                        .update("accountId", nurseId)
                                        .addOnSuccessListener {
                                            Log.d("DEBUG", "User accountId updated with NurseID")

                                            // Step 5: Return success via callback
                                            callback(true)  // Return success
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DEBUG", "Error updating accountId: ", e)
                                            callback(false)  // Return failure
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DEBUG", "Error adding nurse to Nurses collection: ", e)
                                    callback(false)  // Return failure
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DEBUG", "Error adding nurse to Users collection: ", e)
                            callback(false)  // Return failure
                        }
                } else {
                    Log.e("DEBUG", "Error: User creation failed")
                    callback(false)  // Return failure if the user creation failed
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Error creating user with email and password: ", e)
                callback(false)  // Return failure if the user creation failed
            }
    }






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
