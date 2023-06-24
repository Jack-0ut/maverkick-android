package com.example.data.repositories

import android.util.Log
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

    /** Updating the profile picture for the given user**/
    fun updateProfilePicture(userId: String, imageUrl: String) {
        val userDocument = databaseService.db.collection("users").document(userId)
        userDocument.update("profilePicture", imageUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error updating document", e)
            }
    }

    /** Getting the object of a User given the userId **/
    fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}