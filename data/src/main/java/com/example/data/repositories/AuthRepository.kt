package com.example.data.repositories

import android.util.Log
import com.example.data.IDatabaseService
import com.example.data.models.User
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Class that is responsible for authentication and login activities
 * using Firebase Auth
 **/
class AuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth, private val databaseService: IDatabaseService) {

    /** Register the User with email, username and password **/
    fun register(email: String, username: String, password: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = firebaseAuth.currentUser?.uid!!
                val userMap = hashMapOf(
                    "email" to email,
                    "username" to username
                )

                databaseService.db.collection("users").document(userId).set(userMap)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Document written successfully")
                        val user = User(userId, username, email, null)
                        onSuccess(user)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error writing document", e)
                        onFailure(e)
                    }
            } else {
                Log.e("FirebaseAuth", "Registration failed", task.exception)
                onFailure(task.exception!!)
            }
        }
    }


    /** Log in the account **/
    fun login(email: String, password: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        onSuccess(userId)
                    } else {
                        onFailure(Exception("User ID is null"))
                    }
                } else {
                    onFailure(task.exception!!)
                }
            }
    }


    /** Log out of the account **/
    fun logOut() {
        firebaseAuth.signOut()
    }
}