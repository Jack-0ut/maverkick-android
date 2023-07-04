package com.example.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.data.models.User
import com.example.data.repositories.UserRepository
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject


/** Base interface that defines the common functional for the User Profile **/
interface ProfileViewModelInterface {
    val username: LiveData<String>
    val profilePicture: LiveData<Uri>
    fun clearPreferences()
    fun logout()
    fun updateProfilePicture(uri: Uri)
}

class SharedProfileLogic @Inject constructor(
    private val sharedPrefManager: SharedPrefManager,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) {
    fun getUserFromSharedPref(): User? {
        return sharedPrefManager.getUser()
    }

    fun clearPreferences() {
        sharedPrefManager.clearPreferences()
    }

    fun logout() {
        firebaseAuth.signOut()
    }

}