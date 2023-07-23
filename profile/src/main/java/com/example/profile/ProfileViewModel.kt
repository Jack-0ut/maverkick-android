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
        set(value) {
            field = value
            value?.let {
                _username.value = it.username
                if(it.profilePicture != null) {
                    _profilePicture.value = Uri.parse(it.profilePicture)
                }
                sharedPrefManager.saveUser(it)
            }
        }

    init {
        loadUserData()
    }

    override fun clearPreferences() {
        sharedPrefManager.clearPreferences()
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    /** Load user data either from Shared Preferences or from Firestore */
    private fun loadUserData() {
        user = sharedPrefManager.getUser()
        user ?: fetchUserDataFromFirestore()
    }

    /** Fetch user data from Firestore */
    private fun fetchUserDataFromFirestore() {
        firebaseAuth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                try {
                    val fetchedUser = userRepository.getUserById(userId)
                    if (fetchedUser != null) {
                        user = fetchedUser
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    /** Update the profile picture and save to the database **/
    override fun updateProfilePicture(uri: Uri) {
        user?.let {
            viewModelScope.launch {
                try {
                    userRepository.updateProfilePicture(it.userId, uri)
                    // Create a new User object with the updated image URL
                    user = it.copy(profilePicture = uri.toString())
                } catch (e: Exception) {
                    // handle the error
                }
            }
        }
    }
}