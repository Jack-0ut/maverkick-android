package com.example.student.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ProfileSettingsFragment
 * This ViewModel class fetches the Student data from repository
 * and put it's dailyLearningTime and interests into the fragment
 **/
@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val repository: StudentRepository
) : ViewModel() {

    private val _dailyLearningTime = MutableLiveData<String>()
    val dailyLearningTime: LiveData<String> get() = _dailyLearningTime

    private val _interests = MutableLiveData<List<String>>()
    val interests: LiveData<List<String>> get() = _interests

    init {
        fetchStudentData()
    }

    /** Get the current Student object from Firestore **/
    private fun fetchStudentData() {
        viewModelScope.launch {
            repository.getCurrentStudent(
                onSuccess = { student ->
                    // Update the LiveData with the fetched data
                    _dailyLearningTime.value = student.dailyStudyTimeMinutes.toString()
                    _interests.value = student.interests
                },
                onFailure = { error ->
                    // Handle the error case
                    Log.e("ProfileSettingsViewModel", "Error fetching student data: $error")
                }
            )
        }
    }

}
