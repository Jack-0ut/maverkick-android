package com.example.student.viewmodels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ProfileFragment class
 * This class takes the data from the repository
 * and update the username and picture views in the Fragment
 * @param userRepository - UserRepository from which we will be fetching data
 **/
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _userImageURL = MutableLiveData<String>()
    val userImageURL: LiveData<String> get() = _userImageURL

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            userRepository.getCurrentUser(
                onSuccess = { user ->
                    // Update the LiveData with the fetched data
                    _username.value = user.username
                    _userImageURL.value = user.profilePicture
                },
                onFailure = { exception ->
                    // Handle the exception here
                    Log.e(TAG, "Failed to fetch courses: ", exception)
                }

            )
        }
    }

}
