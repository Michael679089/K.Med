package com.example.cs320_hospital_and_medical_android_app

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
// admin imports


class DBHandlerClass() {
    // Firebase Initialization
    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()


    fun addDoctor(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        roomNum: String,
        specialization: String,
        onComplete: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userID = authResult.user?.uid ?: return@addOnSuccessListener

                val userMap = hashMapOf(
                    "accountId" to "",
                    "role" to "doctor"
                )

                db.collection("users").document(userID).set(userMap)
                    .addOnSuccessListener {
                        // Generate unique doctor ID
                        val doctorId = "DID${UUID.randomUUID()}"
                        val doctorMap = hashMapOf(
                            "firebaseUid" to userID,
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "profilePicture" to "",
                            "room" to roomNum,
                            "specialization" to specialization
                        )

                        db.collection("Doctors").document(doctorId).set(doctorMap)
                            .addOnSuccessListener {
                                db.collection("users").document(userID)
                                    .update("accountId", doctorId)
                                    .addOnSuccessListener {
                                        onComplete(true)
                                    }
                                    .addOnFailureListener {
                                        Log.e("DEBUG", "Failed to update user with DoctorID")
                                        onComplete(false)
                                    }
                            }
                            .addOnFailureListener {
                                Log.e("DEBUG", "Failed to create doctor row")
                                onComplete(false)
                            }
                    }
                    .addOnFailureListener {
                        Log.e("DEBUG", "Failed to create user row")
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAuthUserCollisionException) {
                    Log.e("DEBUG", "Email already in use")
                } else {
                    Log.e("DEBUG", "Failed to create auth user: ${e.message}")
                }
                onComplete(false)
            }
    }

    fun addNurse(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onComplete: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userID = authResult.user?.uid ?: return@addOnSuccessListener

                val userMap = hashMapOf(
                    "accountId" to "",
                    "role" to "nurse"
                )

                db.collection("users").document(userID).set(userMap)
                    .addOnSuccessListener {
                        val nurseId = "NID${UUID.randomUUID()}"
                        val nurseMap = hashMapOf(
                            "firebaseUid" to userID,
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "profilePicture" to ""
                        )

                        db.collection("Nurses").document(nurseId).set(nurseMap)
                            .addOnSuccessListener {
                                db.collection("users").document(userID)
                                    .update("accountId", nurseId)
                                    .addOnSuccessListener {
                                        onComplete(true)
                                    }
                                    .addOnFailureListener {
                                        Log.e("DEBUG", "Failed to update user with NurseID")
                                        onComplete(false)
                                    }
                            }
                            .addOnFailureListener {
                                Log.e("DEBUG", "Failed to create nurse row")
                                onComplete(false)
                            }
                    }
                    .addOnFailureListener {
                        Log.e("DEBUG", "Failed to create user row")
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                if (e is FirebaseAuthUserCollisionException) {
                    Log.e("DEBUG", "Email already in use")
                } else {
                    Log.e("DEBUG", "Failed to create auth user: ${e.message}")
                }
                onComplete(false)
            }
    }

    fun deleteAccountByUserId(firebaseUid: String, role: String, callback: (Boolean) -> Unit) {
        val roleCollection = when (role) {
            "patient" -> "Patients"
            "doctor" -> "Doctors"
            "nurse" -> "Nurses"
            "patient-notreg" -> {
                db.collection("users").document(firebaseUid).delete()
                    .addOnSuccessListener {
                        Log.d("DEBUG", "Successfully deleted a patient-notreg account")
                        callback(true)
                    }
                    .addOnFailureListener {
                        Log.e("DEBUG", "ERROR: can't delete '$firebaseUid' patient-notreg account")
                        callback(false)
                    }
                return
            }
            "admin" -> {
                Log.e("DEBUG", "ERROR: Admin Account Deletion is not allowed here. Do it in Firebase Console.")
                callback(false)
                return
            }
            else -> {
                Log.e("DEBUG", "ERROR: Unknown role '$role'")
                callback(false)
                return
            }
        }

        // Fetch user document
        db.collection("users").document(firebaseUid).get()
            .addOnSuccessListener { userRow ->
                if (!userRow.exists()) {
                    Log.e("DEBUG", "ERROR: Can't find $firebaseUid user row")
                    callback(false)
                    return@addOnSuccessListener
                }

                val accountId = userRow["accountId"].toString()

                // Delete role-specific document
                db.collection(roleCollection).document(accountId).delete()
                    .addOnSuccessListener {
                        Log.d("DEBUG", "Successfully deleted $role document: $accountId")
                    }
                    .addOnFailureListener {
                        Log.e("DEBUG", "ERROR: Can't delete $role document: $accountId")
                    }

                // Then delete user document
                db.collection("users").document(firebaseUid).delete()
                    .addOnSuccessListener {
                        Log.d("DEBUG", "Successfully deleted user document: $firebaseUid")
                        Log.d("DEBUG", "Note: To delete the Firebase Auth account, you need to do it via the Firebase Console or backend")
                        callback(true)
                    }
                    .addOnFailureListener {
                        Log.e("DEBUG", "ERROR: Can't delete user document: $firebaseUid")
                        callback(false)
                    }
            }
            .addOnFailureListener {
                Log.e("DEBUG", "ERROR: Failed to fetch user document: $firebaseUid")
                callback(false)
            }
    }


    fun fetchUserList(callback: (Array<Array<String>>) -> Unit) {
        Log.d("DEBUG", "🟦 Starting to fetch user list...")

        db.collection("users").get()
            .addOnSuccessListener { usersSnapshot ->
                Log.d("DEBUG", "🟩 Users fetched successfully. Number of users: ${usersSnapshot.documents.size}")

                if (usersSnapshot.documents.isNotEmpty()) {
                    val userList = mutableListOf<Array<String>>()
                    var processedCount = 0

                    for (userDocument in usersSnapshot.documents) {
                        val infoEntry = mutableListOf<String>()

                        val userID = userDocument.id
                        val accountId = userDocument["accountId"].toString()
                        val role = userDocument["role"].toString()

                        infoEntry.add(userID)
                        infoEntry.add(accountId)
                        infoEntry.add(role)

                        val collectionName = when (role) {
                            "patient" -> "Patients"
                            "nurse" -> "Nurses"
                            "doctor" -> "Doctors"
                            "admin" -> "Admins"
                            "patient-notreg" -> null
                            else -> null
                        }

                        if (collectionName != null) {
                            db.collection(collectionName).document(accountId).get()
                                .addOnSuccessListener { profileDoc ->
                                    val firstName = profileDoc.getString("firstName") ?: ""
                                    val lastName = profileDoc.getString("lastName") ?: ""
                                    val fullName = "$firstName $lastName".trim()

                                    infoEntry.add(if (fullName.isNotEmpty()) fullName else "No profile linked")
                                    userList.add(infoEntry.toTypedArray())
                                    processedCount++

                                    if (processedCount == usersSnapshot.documents.size) {
                                        callback(userList.toTypedArray())
                                    }
                                }
                                .addOnFailureListener {
                                    infoEntry.add("No profile linked")
                                    userList.add(infoEntry.toTypedArray())
                                    processedCount++

                                    if (processedCount == usersSnapshot.documents.size) {
                                        callback(userList.toTypedArray())
                                    }
                                }
                        } else {
                            // For 'patient-notreg'
                            infoEntry.add("No profile linked")
                            userList.add(infoEntry.toTypedArray())
                            processedCount++

                            if (processedCount == usersSnapshot.documents.size) {
                                callback(userList.toTypedArray())
                            }
                        }
                    }
                } else {
                    Log.d("DEBUG", "❗ No users found in the 'users' collection.")
                    callback(emptyArray())
                }

            }
            .addOnFailureListener { e ->
                Log.d("DEBUG", "❌ Error fetching users: ${e.message}")
                callback(emptyArray())
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

    fun getNameOfLoggedInUser(callback: (String) -> Unit) {
        val firebaseUid = auth.currentUser?.uid
        if (firebaseUid == null) {
            Log.e("DEBUG", "No logged-in user found")
            callback("")
            return
        }

        db.collection("users").document(firebaseUid).get()
            .addOnSuccessListener { userDoc ->
                if (!userDoc.exists()) {
                    Log.e("DEBUG", "User document not found")
                    callback("")
                    return@addOnSuccessListener
                }

                val accountId = userDoc.getString("accountId") ?: ""
                val role = userDoc.getString("role")?.lowercase() ?: ""

                val collectionName = when (role) {
                    "patient" -> "Patients"
                    "nurse" -> "Nurses"
                    "doctor" -> "Doctors"
                    else -> {
                        Log.e("DEBUG", "Invalid role: $role")
                        callback("")
                        return@addOnSuccessListener
                    }
                }

                db.collection(collectionName).document(accountId).get()
                    .addOnSuccessListener { roleDoc ->
                        if (roleDoc.exists()) {
                            val fullName = "${roleDoc.getString("firstName") ?: ""} ${roleDoc.getString("lastName") ?: ""}".trim()
                            Log.d("DEBUG", "User Full Name: $fullName")
                            callback(fullName)
                        } else {
                            Log.e("DEBUG", "No $role document found with ID: $accountId")
                            callback("")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DEBUG", "Error fetching $role document", e)
                        callback("")
                    }

            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Error fetching user document", e)
                callback("")
            }
    }

    fun getAccountID(callback: (String) -> Unit) {
        val firebaseUid = auth.currentUser?.uid
        var accountId = ""

        if (firebaseUid == null) {
            Log.e("DEBUG", "No logged-in user found")
            callback("")
            return
        }

        db.collection("users").document(firebaseUid).get()
            .addOnSuccessListener { userRow ->
                if (userRow.exists()) {
                    accountId = userRow["accountId"].toString()
                    callback(accountId)
                }
                callback("")
            }
            .addOnFailureListener {
                Log.e("DEBUG", "ERROR: Can't find user.")
                callback("")
            }
    }

    fun getRoleOfLoggedInUser(callback: (String) -> Unit) {
        val firebaseUid = auth.currentUser?.uid
        if (firebaseUid == null) {
            Log.e("DEBUG", "No logged-in user found")
            callback("")
            return
        }

        db.collection("users").document(firebaseUid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userRole = document.getString("role")?.lowercase() ?: ""
                    Log.d("DEBUG", "Fetched user role: $userRole")
                    callback(userRole)
                } else {
                    Log.e("DEBUG", "No user document found for UID: $firebaseUid")
                    callback("")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Failed to fetch user role", e)
                callback("")
            }
    }

}
