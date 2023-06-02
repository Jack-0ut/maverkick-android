package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.Teacher
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

/**
 * Class responsible for the Teacher objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class TeacherRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new Teacher to the database **/
    fun addTeacher(){

    }

    /** Getting a Teacher from database by the user ID **/
    fun getTeacherById(userId: String, onSuccess: (Teacher) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("teachers").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val teacher = documentSnapshot.toObject(Teacher::class.java)
                if (teacher != null) {
                    onSuccess(teacher)
                } else {
                    onFailure(Exception("No such teacher"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Getting the current Teacher from database **/
    fun getCurrentTeacher(onSuccess: (Teacher) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            getTeacherById(firebaseUser.uid, onSuccess, onFailure)
        } else {
            onFailure(Exception("No current user"))
        }
    }
}
