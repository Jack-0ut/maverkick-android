package com.example.app.onboarding.student

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.StudentRepository
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to store the data Student entered
 * during onboarding process
 **/
@HiltViewModel
class StudentOnboardingViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPrefManager: SharedPrefManager
): ViewModel(){

    val age = MutableLiveData<Int>()
    val dailyLearningTime = MutableLiveData<Int>()
    val skills = MutableLiveData<List<String>>()

    private val _createStudentResult = MutableLiveData<Result<String>>()
    val createStudentResult: LiveData<Result<String>> = _createStudentResult

    fun createStudentAndAddToFirestore() {
        val ageValue = age.value
        val dailyLearningTimeValue = dailyLearningTime.value
        val skillsValue = skills.value

        if (ageValue != null && dailyLearningTimeValue != null && skillsValue != null) {
            viewModelScope.launch {
                // Get current user's ID
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid
                    // Create student and add to Firestore
                    val result = studentRepository.addStudent(userId, ageValue, dailyLearningTimeValue, skillsValue)

                    if (result.isSuccess) {
                        // If successful, save studentId and role to Shared Preferences
                        val studentId = result.getOrNull()
                        sharedPrefManager.saveActiveRole("student")
                        sharedPrefManager.saveStudentId(studentId.toString())
                    }
                    _createStudentResult.value = result
                } else {
                    _createStudentResult.value = Result.failure(Exception("No current user"))
                }
            }
        } else {
            Log.w(TAG, "Not all fields have been filled in")
        }
    }

}
