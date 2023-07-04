package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.FirebaseStudent
import com.example.data.models.Student
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
        // Create the FirebaseStudent object
        val firebaseStudent = FirebaseStudent(age, dailyStudyTimeMinutes, interests)
        // Add the Student to the database
        return try {
            databaseService.db.collection("students").document(userId).set(firebaseStudent).await()
            // Convert the FirebaseStudent to a Student, including the userId
            val student = firebaseStudent.toStudent(userId)
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get a Student by their user ID **/
    suspend fun getStudentById(userId: String): Result<Student> {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firebaseStudent = FirebaseStudent(
                            age = document.getLong("age")?.toInt() ?: 0,
                            dailyStudyTimeMinutes = document.getLong("dailyStudyTimeMinutes")?.toInt() ?: 0,
                            interests = document.get("interests") as List<String>? ?: listOf()
                        )
                        // Convert the FirebaseStudent to a Student, using the userId as the studentId
                        val student = firebaseStudent.toStudent(userId)
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
