package com.example.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the ProfileFragment class
 * This class takes the data from the repository
 * and update the username and picture views in the Fragment
 **/
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager  // Inject SharedPrefManager here
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _userImageURL = MutableLiveData<String>()
    val userImageURL: LiveData<String> get() = _userImageURL

    init {
        loadUserDataFromSharedPref() // Load user data from shared preferences
    }

    private fun loadUserDataFromSharedPref() {
        val user = sharedPrefManager.getUser()
        user?.let {
            _username.value = it.username
            //_userImageURL.value = it.profilePicture
        }
    }
}

