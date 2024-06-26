package com.maverkick.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maverkick.data.auth.AuthenticationService
import com.maverkick.data.repositories.StudentRepository
import com.maverkick.data.repositories.UserRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import com.maverkick.profile.ProfileViewModel
import com.maverkick.profile.ProfileViewModelInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Specific Profile ViewModel for the Teacher **/
@HiltViewModel
class TeacherProfileViewModel @Inject constructor(
    sharedPrefManager: SharedPrefManager,
    userRepository: UserRepository,
    authService: AuthenticationService,
    private val studentRepository: StudentRepository
) : ProfileViewModel(sharedPrefManager, userRepository, authService), ProfileViewModelInterface {

    private val _studentAccountExists = MutableLiveData<Boolean>()
    val studentAccountExists: LiveData<Boolean> get() = _studentAccountExists

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData

    /** Check if student exists and we could switch to that view **/
    suspend fun checkStudentAccountExists(): Boolean {
        val userId = authService.currentUser?.uid ?: return false
        return try {
            val studentExists = userRepository.checkIfStudentExists(userId)
            if (studentExists) {
                val studentResult = studentRepository.getStudentById(userId)
                if (studentResult.isSuccess) {
                    val student = studentResult.getOrNull()
                    if (student != null) {
                        sharedPrefManager.saveStudent(student)
                        sharedPrefManager.saveActiveRole("student")
                        return true
                    } else {
                        _errorLiveData.postValue("Failed to retrieve student data")
                    }
                } else {
                    val exception = studentResult.exceptionOrNull()
                    _errorLiveData.postValue("Error retrieving student data: $exception")
                }
            }
            false
        } catch (e: Exception) {
            _errorLiveData.postValue("An error occurred: ${e.message}")
            false
        }
    }
}
