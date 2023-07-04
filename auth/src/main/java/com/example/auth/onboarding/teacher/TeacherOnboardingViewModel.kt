package com.example.auth.onboarding.teacher

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Teacher
import com.example.data.repositories.TeacherRepository
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to store the data Teacher entered during onboarding process
 * and save the registered Teacher to the database
 **/
@HiltViewModel
class TeacherOnboardingViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository,
    private val firebaseAuth: FirebaseAuth,
    private val sharedPrefManager: SharedPrefManager,
): ViewModel(){

    val fullName = MutableLiveData<String>()
    val country = MutableLiveData<String>()
    val expertiseList = MutableLiveData<List<String>>()

    private val _createTeacherResult= MutableLiveData<Result<Teacher>>()
    val createTeacherResult: LiveData<Result<Teacher>> = _createTeacherResult

    fun createTeacherAndAddToFirestore() {
        val fullNameValue = fullName.value
        val expertiseListValue = expertiseList.value
        val countryValue = country.value

        if (fullNameValue != null && countryValue != null && expertiseListValue != null) {
            viewModelScope.launch {
                // Get current user
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid
                    // Create teacher and add to Firestore
                    val result = runCatching {
                        teacherRepository.addTeacher(userId, fullNameValue,countryValue, expertiseListValue)
                    }
                    result.onSuccess { teacher ->
                        // If successful, save teacher and set current role to the teacher
                        teacher.getOrNull()?.let {
                            sharedPrefManager.saveActiveRole("teacher")
                            sharedPrefManager.saveTeacher(it)
                        }
                    }

                    _createTeacherResult.postValue(result.getOrNull())
                } else {
                    _createTeacherResult.postValue(Result.failure(Exception("No current user")))
                }
            }
        }else{
            Log.w(ContentValues.TAG, "Not all fields have been filled in")
        }
    }
}

