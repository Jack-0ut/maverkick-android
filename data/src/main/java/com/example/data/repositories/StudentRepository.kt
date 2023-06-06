package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.Student
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Class responsible for the Student objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class StudentRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new Student to the database and if everything is OK, return the student **/
    suspend fun addStudent(userId: String, age: Int, dailyStudyTimeMinutes: Int, interests: List<String>): Result<Student> {
        // Create the Student object
        val student = Student("", userId, age, dailyStudyTimeMinutes, interests)
        // Add the Student to the database
        return try {
            val documentReference = databaseService.db.collection("students").add(student).await()
            val studentId = documentReference.id
            // Update the student document to include the studentId
            databaseService.db.collection("students").document(studentId).update("studentId", studentId).await()
            // Set the studentId in the student object
            student.studentId = studentId
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /** Getting the current Student from database **/
    suspend fun getCurrentStudent(): Result<Student> {
        val firebaseUser = Firebase.auth.currentUser
        return if (firebaseUser != null) {
            getStudentByUserId(firebaseUser.uid)
        } else {
            Result.failure(Exception("No current Student"))
        }
    }

    /** Get a Student by their user ID **/
    suspend fun getStudentByUserId(userId: String): Result<Student> {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("students")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val student = Student(
                            studentId = document.getString("studentId") ?: "",
                            userId = document.getString("userId") ?: "",
                            age = document.getLong("age")?.toInt() ?: 0,
                            dailyStudyTimeMinutes = document.getLong("dailyStudyTimeMinutes")?.toInt() ?: 0,
                            interests = document.get("interests") as List<String>? ?: listOf()
                        )
                        continuation.resume(Result.success(student))
                    } else {
                        continuation.resume(Result.failure(Exception("No such student")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }



}