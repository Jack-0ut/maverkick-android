package com.maverkick.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.auth.AuthenticationService
import com.maverkick.data.models.User
import com.maverkick.data.repositories.UserRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import kotlinx.coroutines.launch

/**
 * ViewModel for the ProfileFragment class
 * This class takes the data from the repository
 * and update the username and picture views in the Fragment
 **/
abstract class ProfileViewModel(
    protected val sharedPrefManager: SharedPrefManager,
    protected val userRepository: UserRepository,
    protected val authService: AuthenticationService
) : ViewModel(), ProfileViewModelInterface {

    private val _username = MutableLiveData<String>()
    override val username: LiveData<String> get() = _username

    private val _profilePicture = MutableLiveData<Uri>()
    override val profilePicture: LiveData<Uri> get() = _profilePicture

    private var user: User? = null
        set(value) {
            field = value
            value?.let {
                _username.postValue(it.username)
                it.profilePicture?.let { imageUrl ->
                    _profilePicture.postValue(Uri.parse(imageUrl))
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
        authService.logout()  // Use authService's logout method
    }

    /** Load user data either from Shared Preferences or from Firestore */
    private fun loadUserData() {
        user = sharedPrefManager.getUser()
        if (user == null) {
            fetchUserDataFromFirestore()
        }
    }

    /** Fetch user data from Firestore */
    private fun fetchUserDataFromFirestore() {
        val userId = authService.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                userRepository.getUserById(userId)?.let { fetchedUser ->
                    user = fetchedUser
                }
            } catch (e: Exception) {
                // handle the error, consider logging or forwarding this error to an error handling mechanism
            }
        }
    }

    /** Update the profile picture and save to the database **/
    override fun updateProfilePicture(uri: Uri) {
        val currentUser = user ?: return
        viewModelScope.launch {
            try {
                userRepository.updateProfilePicture(currentUser.userId, uri)
                user = currentUser.copy(profilePicture = uri.toString())
            } catch (e: Exception) { }
        }
    }
}
