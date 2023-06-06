package com.example.student.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ProfileSettingsFragment
 * This ViewModel class fetches the Student data from repository
 * and put it's dailyLearningTime and interests into the fragment
 **/
@HiltViewModel
class StudentProfileSettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPrefManager
) : ViewModel() {

    private val _dailyLearningTime = MutableLiveData<String>()
    val dailyLearningTime: LiveData<String> get() = _dailyLearningTime

    private val _interests = MutableLiveData<List<String>>()
    val interests: LiveData<List<String>> get() = _interests

    init {
        fetchStudentData()
    }

    /** Get the current Student data from Firestore **/
    private fun fetchStudentData() {
        viewModelScope.launch {
            val student = sharedPreferences.getStudent()
            if (student != null) {
                _dailyLearningTime.value = student.dailyStudyTimeMinutes.toString()
                _interests.value = student.interests
            }
        }
    }

}
