package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.FirebaseTeacher
import com.example.data.models.Teacher
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    suspend fun addTeacher(userId: String, fullNameValue: String, expertiseValue: List<String>): Result<Teacher> {
        val firebaseTeacher = FirebaseTeacher(userId, fullNameValue, expertiseValue)
        return try {
            val documentReference = databaseService.db.collection("teachers").add(firebaseTeacher).await()
            val teacherId = documentReference.id
            val teacher = firebaseTeacher.toTeacher(teacherId)
            Result.success(teacher)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get a Teacher by their user ID **/
    suspend fun getTeacherByUserId(userId: String): Result<Teacher> {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("teachers")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val firebaseTeacher = FirebaseTeacher(
                            userId = document.getString("userId") ?: "",
                            fullName = document.getString("fullName") ?: "",
                            expertise = document.get("expertise") as List<String>? ?: listOf()
                        )
                        val teacher = firebaseTeacher.toTeacher(document.id)
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

    /** Getting the current Teacher from database **/
    suspend fun getCurrentTeacher(): Result<Teacher> {
        val firebaseUser = Firebase.auth.currentUser
        return if (firebaseUser != null) {
            getTeacherByUserId(firebaseUser.uid)
        } else {
            Result.failure(Exception("No current Teacher"))
        }
    }
}
