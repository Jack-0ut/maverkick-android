package com.maverkick.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import com.maverkick.data.models.User
import com.maverkick.data.repositories.UserRepository
import com.maverkick.data.sharedpref.SharedPrefManager
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