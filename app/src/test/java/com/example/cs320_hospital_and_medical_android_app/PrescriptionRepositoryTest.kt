package com.example.cs320_hospital_and_medical_android_app.repository

import com.example.cs320_hospital_and_medical_android_app.models.PrescriptionModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import com.google.android.gms.tasks.Task


@OptIn(ExperimentalCoroutinesApi::class)
class PrescriptionRepositoryTest {

    private lateinit var db: FirebaseFirestore
    private lateinit var repository: PrescriptionRepository
    private lateinit var documentRef: DocumentReference

    @Before
    fun setup() {
        db = mock(FirebaseFirestore::class.java)
        documentRef = mock(DocumentReference::class.java)
        repository = PrescriptionRepository(db)
    }

    @Test
    fun `test savePrescription success`() = runTest {
        val prescription = PrescriptionModel(
            id = "test123",
            doctorId = "doc001",
            doctorName = "Dr. Test",
            patientId = "pat001",
            date = "April 6, 2025",
            details = "Take 1 tablet daily.",
            createdAt = Timestamp.now()
        )

        val prescriptionsCollection = mock(CollectionReference::class.java)
        `when`(db.collection("prescriptions")).thenReturn(prescriptionsCollection)
        `when`(prescriptionsCollection.document("test123")).thenReturn(documentRef)

        // Mock Task that simulates successful Firebase `set()`
        @Suppress("UNCHECKED_CAST")
        val mockedTask = mock(Task::class.java) as Task<Void>
        `when`(mockedTask.isComplete).thenReturn(true)
        `when`(mockedTask.isSuccessful).thenReturn(true)
        `when`(mockedTask.result).thenReturn(null)
        `when`(documentRef.set(prescription)).thenReturn(mockedTask)

        val result = repository.savePrescription(prescription)
        assertTrue(result)
    }
}
