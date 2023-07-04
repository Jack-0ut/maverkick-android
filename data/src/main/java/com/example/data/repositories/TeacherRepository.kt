package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.FirebaseTeacher
import com.example.data.models.Teacher
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Class responsible for the Teacher objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class TeacherRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new Teacher into the database */
    suspend fun addTeacher(userId: String, fullNameValue: String, country: String, expertiseValue: List<String>): Result<Teacher> {
        val firebaseTeacher = FirebaseTeacher(fullNameValue, country, expertiseValue)
        return try {
            databaseService.db.collection("teachers").document(userId).set(firebaseTeacher).await()
            val teacher = firebaseTeacher.toTeacher(userId)
            Result.success(teacher)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /** Get a Teacher by their ID **/
    suspend fun getTeacherById(teacherId: String): Result<Teacher> {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("teachers")
                .document(teacherId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val firebaseTeacher = FirebaseTeacher(
                            fullName = documentSnapshot.getString("fullName") ?: "",
                            country = documentSnapshot.getString("country") ?: "",
                            expertise = documentSnapshot.get("expertise") as List<String>? ?: listOf()
                        )
                        val teacher = firebaseTeacher.toTeacher(documentSnapshot.id)
                        continuation.resume(Result.success(teacher))
                    } else {
                        continuation.resume(Result.failure(Exception("No such teacher")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

}
