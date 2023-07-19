package com.example.student.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.StudentRepository
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
    private val sharedPreferences: SharedPrefManager,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _dailyLearningTime = MutableLiveData<String>()
    val dailyLearningTime: LiveData<String> get() = _dailyLearningTime

    private val _interests = MutableLiveData<List<String>>()
    val interests: LiveData<List<String>> get() = _interests

    val snackbarMessage = MutableLiveData<String>()

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

    /** Update the desired learning time per day **/
    fun updateDailyLearningTime(newTime: String) {
        val studentId = sharedPreferences.getStudent()!!.studentId
        viewModelScope.launch {
            val result = studentRepository.updateDailyStudyTime(studentId, newTime.toInt())
            if (result.isSuccess) {
                _dailyLearningTime.value = newTime
            } else {
                snackbarMessage.value = "Failed to update daily learning time"
            }
        }
    }


}
