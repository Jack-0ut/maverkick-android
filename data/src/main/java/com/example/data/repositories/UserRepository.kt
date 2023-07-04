package com.example.data.repositories

import android.net.Uri
import com.example.data.IDatabaseService
import com.example.data.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Class responsible for the User objects interaction with
 * the Cloud Database
 * @param databaseService - database to which we're connecting
 **/
class UserRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val firebaseStorage: FirebaseStorage
)
{
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

    /** Save new profile picture to the firebase storage under the userId name */
    fun updateProfilePicture(userId: String, uri: Uri) {
        val storageRef = firebaseStorage.reference
        val profilePictureRef = storageRef.child("profilePictures/$userId")

        val uploadTask = profilePictureRef.putFile(uri)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            profilePictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                updateFirebaseProfilePicture(userId, downloadUri.toString())
            }
        }
    }

    /** Update the url of the profile picture inside the firestore users collection **/
    private fun updateFirebaseProfilePicture(userId: String, profilePictureUrl: String) {
        val userRef = databaseService.db.collection("users").document(userId)
        userRef
            .update("profilePicture", profilePictureUrl)
            .addOnSuccessListener {
                // Successfully updated the document
            }
            .addOnFailureListener {
                // Handle failure
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

    /** Check if teacher account exists for this user**/
    suspend fun checkIfTeacherExists(userId: String): Boolean {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("teachers")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    continuation.resume(document.exists())
                }
                .addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }

    /** Check if student account exists for this user**/
    suspend fun checkIfStudentExists(userId: String): Boolean {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    continuation.resume(document.exists())
                }
                .addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }


}