package com.example.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.User
import com.example.data.repositories.UserRepository
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel for the ProfileFragment class
 * This class takes the data from the repository
 * and update the username and picture views in the Fragment
 **/
abstract class ProfileViewModel(
    protected val sharedPrefManager: SharedPrefManager,
    protected val userRepository: UserRepository,
    protected val firebaseAuth: FirebaseAuth
) : ViewModel(), ProfileViewModelInterface {

    private val _username = MutableLiveData<String>()
    override val username: LiveData<String> get() = _username

    private val _profilePicture = MutableLiveData<Uri>()
    override val profilePicture: LiveData<Uri> get() = _profilePicture

    private var user: User? = null

    init {
        loadUserDataFromSharedPref()
    }

    override fun clearPreferences() {
        sharedPrefManager.clearPreferences()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    /** Get the user object from the shared preferences */
    private fun loadUserDataFromSharedPref() {
        user = sharedPrefManager.getUser()
        user?.let {
            _username.value = it.username
            if(it.profilePicture != null) {
                _profilePicture.value = Uri.parse(it.profilePicture)
            }
        }
    }

    /** Update the profile picture and save to the database **/
    override fun updateProfilePicture(uri: Uri) {
        user?.let {
            viewModelScope.launch {
                try {
                    userRepository.updateProfilePicture(it.userId, uri)
                    // Update the LiveData value
                    _profilePicture.value = uri

                    // Create a new User object with the updated image URL
                    val updatedUser = it.copy(profilePicture = uri.toString())
                    // Update the User object in shared preferences
                    sharedPrefManager.saveUser(updatedUser)
                    // Update the User object in the ViewModel
                    user = updatedUser
                } catch (e: Exception) {
                    // handle the error
                }
            }
        }
    }
}


