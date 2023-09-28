package com.maverkick.text_lesson.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.api.CourseGenerationResponse
import com.maverkick.data.repositories.StudentRepository
import com.maverkick.data.repositories.TextCourseRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PersonalizedCourseCreationViewModel @Inject constructor(
    private val textCourseRepository: TextCourseRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _courseGenerationResult = MutableLiveData<Result<Response<CourseGenerationResponse>>>()
    val courseGenerationResult: LiveData<Result<Response<CourseGenerationResponse>>> = _courseGenerationResult

    private val _courseGenerationTries = MutableLiveData<Result<Int>>()
    val courseGenerationTries: LiveData<Result<Int>> get() = _courseGenerationTries

    private val _canGenerateCourse = MutableLiveData<Boolean>()
    val canGenerateCourse: LiveData<Boolean> get() = _canGenerateCourse

    private var courseDescriptionPrompt: String? = null
    private var selectedLanguage: String? = null

    fun generateCourse(userId: String, prompt: String, language: String) {
        viewModelScope.launch {
            val result = try {
                Result.success(textCourseRepository.generateCourse(userId, prompt, language))
            } catch (e: Exception) {
                Result.failure(e)
            }
            _courseGenerationResult.postValue(result)
        }
    }

    fun initiateCourseGeneration(userId: String, prompt: String, language: String) {
        viewModelScope.launch {
            val triesResult = fetchCourseGenerationTries()
            if (triesResult.isSuccess) {
                if (triesResult.getOrNull()!! > 0) {  // Ensure there are tries left
                    generateCourse(userId, prompt, language)
                } else {
                    _courseGenerationResult.postValue(Result.failure(Exception("No tries left")))
                }
            } else {
                _courseGenerationResult.postValue(Result.failure(triesResult.exceptionOrNull()!!))
            }
        }
    }

    private suspend fun fetchCourseGenerationTries(): Result<Int> {
        return try {
            val studentId = sharedPrefManager.getStudent()?.studentId ?: throw IllegalStateException("Student ID not found.")
            studentRepository.getCourseGenerationTries(studentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserId(): String? {
        return sharedPrefManager.getStudent()?.studentId
    }
}
