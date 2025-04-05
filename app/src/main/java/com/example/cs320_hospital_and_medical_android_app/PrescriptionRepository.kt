// PrescriptionRepository.kt (new file for logic separation)
package com.example.cs320_hospital_and_medical_android_app.repository

import com.example.cs320_hospital_and_medical_android_app.models.PrescriptionModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PrescriptionRepository(private val db: FirebaseFirestore) {

    suspend fun savePrescription(prescription: PrescriptionModel): Boolean {
        return try {
            db.collection("prescriptions").document(prescription.id).set(prescription).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
