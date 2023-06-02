package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

/**
 * Class responsible for the User objects interaction with
 * the Cloud Database
 * @param databaseService - database to which we're connecting
 **/
class UserRepository @Inject constructor(private val databaseService: IDatabaseService) {


    /** Getting the object of current User **/
    fun getCurrentUser(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            databaseService.db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("No such document"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("No current user"))
        }
    }
}