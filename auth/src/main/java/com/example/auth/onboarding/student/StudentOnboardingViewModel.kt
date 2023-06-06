package com.example.auth.onboarding.student

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Student
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
    val interests = MutableLiveData<List<String>>()

    private val _createStudentResult = MutableLiveData<Result<Student>>()
    val createStudentResult: LiveData<Result<Student>> = _createStudentResult


    fun createStudentAndAddToFirestore() {
        val ageValue = age.value
        val dailyLearningTimeValue = dailyLearningTime.value
        val interestsValue = interests.value

        if (ageValue != null && dailyLearningTimeValue != null && interestsValue != null) {
            viewModelScope.launch {
                // Get current user's ID
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid
                    // Create student and add to Firestore
                    val result = runCatching {
                        studentRepository.addStudent(userId, ageValue, dailyLearningTimeValue, interestsValue)
                    }

                    result.onSuccess { student ->
                        // If successful, save student and set current role to the student
                        student.getOrNull()?.let {
                            sharedPrefManager.saveActiveRole("student")
                            sharedPrefManager.saveStudent(it)
                        }
                    }

                    _createStudentResult.postValue(result.getOrNull())
                } else {
                    _createStudentResult.postValue(Result.failure(Exception("No current user")))
                }
            }
        } else {
            Log.w(TAG, "Not all fields have been filled in")
        }
    }
}
