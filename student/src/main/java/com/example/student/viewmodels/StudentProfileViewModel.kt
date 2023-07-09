package com.example.student.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.data.repositories.TeacherRepository
import com.example.data.repositories.UserRepository
import com.example.data.sharedpref.SharedPrefManager
import com.example.profile.ProfileViewModel
import com.example.profile.ProfileViewModelInterface
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Specific Profile ViewModel for the Student **/
@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    sharedPrefManager: SharedPrefManager,
    userRepository: UserRepository,
    firebaseAuth: FirebaseAuth,
    private val teacherRepository: TeacherRepository
) : ProfileViewModel(sharedPrefManager, userRepository, firebaseAuth), ProfileViewModelInterface {

    private val _teacherAccountExists = MutableLiveData<Boolean>()
    val teacherAccountExists: LiveData<Boolean> get() = _teacherAccountExists

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData

    /** Check if teacher account exists for the user**/
    suspend fun checkTeacherAccountExists(): Boolean {
        val userId = firebaseAuth.currentUser?.uid ?: return false
        return try {
            val teacherExists = userRepository.checkIfTeacherExists(userId)
            if (teacherExists) {
                val teacherResult = teacherRepository.getTeacherById(userId)
                if (teacherResult.isSuccess) {
                    val teacher = teacherResult.getOrNull()
                    if (teacher != null) {
                        sharedPrefManager.saveTeacher(teacher)
                        sharedPrefManager.saveActiveRole("teacher")
                        return true
                    } else {
                        _errorLiveData.postValue("Failed to retrieve teacher data")
                    }
                } else {
                    val exception = teacherResult.exceptionOrNull()
                    _errorLiveData.postValue("Error retrieving teacher data: $exception")
                }
            }
            false
        } catch (e: Exception) {
            _errorLiveData.postValue("An error occurred: ${e.message}")
            false
        }
    }

}
