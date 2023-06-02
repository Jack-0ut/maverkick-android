package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.Student
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Class responsible for the Student objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class StudentRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new Student to the database and if everything is OK, return the studentId**/
    suspend fun addStudent(userId: String, age: Int, dailyStudyTimeMinutes: Int, interests: List<String>): Result<String> {
        // Create the Student object
        val student = Student("", userId, age, dailyStudyTimeMinutes, interests)

        // Add the Student to the database
        return try {
            val documentReference = databaseService.db.collection("students").add(student).await()
            val studentId = documentReference.id
            // Update the student document to include the studentId
            databaseService.db.collection("students").document(studentId).update("studentId", studentId).await()
            Result.success(studentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Getting the current Student from database **/
    fun getCurrentStudent(onSuccess: (Student) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            getStudentById(userId, onSuccess, onFailure)
        } else {
            onFailure(Exception("No current user"))
        }
    }

    /** Get a Student by their user ID **/
    fun getStudentById(userId: String, onSuccess: (Student) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("students").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val student = documentSnapshot.toObject(Student::class.java)
                if (student != null) {
                    onSuccess(student)
                } else {
                    onFailure(Exception("No such document"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
