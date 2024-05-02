package com.maverkick.student.viewmodels

import androidx.lifecycle.MutableLiveData
import com.maverkick.data.auth.AuthenticationService
import com.maverkick.data.repositories.TeacherRepository
import com.maverkick.data.repositories.UserRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import com.maverkick.profile.ProfileViewModel
import com.maverkick.profile.ProfileViewModelInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Specific Profile ViewModel for the Student **/
@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    sharedPrefManager: SharedPrefManager,
    userRepository: UserRepository,
    authService: AuthenticationService,
    private val teacherRepository: TeacherRepository
) : ProfileViewModel(sharedPrefManager, userRepository, authService), ProfileViewModelInterface {

    private val _errorLiveData = MutableLiveData<String>()

    /** Check if teacher account exists for the user**/
    suspend fun checkTeacherAccountExists(): Boolean {
        val userId = authService.currentUser?.uid ?: return false
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
