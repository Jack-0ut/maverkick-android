package com.maverkick.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.maverkick.data.models.Course
import com.maverkick.data.repositories.CourseRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Teacher HomeFragment class.
 * This ViewModel class would interact with our data source and
 * expose LiveData objects that Fragment can observe to update the UI.
 * So, basically it's fetching the list of courses from the database and
 * display it to the teacher, so he could edit or add new lessons to it
 **/
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var listenerRegistrations = mutableListOf<ListenerRegistration>()

    init {
        fetchCourses()
    }

    /** Fetch the list of the courses for the current teacher **/
    private fun fetchCourses() = viewModelScope.launch {
        clearListeners()
        fetchTeacherCourses()
    }

    private suspend fun fetchTeacherCourses() {
        val teacherId = sharedPrefManager.getTeacher()?.teacherId
        if (teacherId != null) {
            try {
                val courseList = courseRepository.getTeacherCourses(teacherId)
                courseList.forEach { course ->
                    registerCourseUpdateListener(course)
                }
                _courses.value = courseList
            } catch (exception: Exception) {
                handleException(exception)
            }
        } else {
            _error.value = "Teacher ID is null"
        }
    }

    private fun registerCourseUpdateListener(course: Course) {
        val registration = courseRepository.getCourseByIdRealTime(course.courseId,
            onSuccess = { updatedCourse ->
                updatedCourse?.let { handleUpdatedCourse(it) }
            },
            onFailure = { exception ->
                handleException(exception)
            })
        listenerRegistrations.add(registration)
    }

    private fun handleUpdatedCourse(updatedCourse: Course) {
        val updatedCourses = _courses.value.toMutableList()
        val index = updatedCourses.indexOfFirst { it.courseId == updatedCourse.courseId }
        if (index != -1) {
            updatedCourses[index] = updatedCourse
            _courses.value = updatedCourses
        }
    }

    private fun handleException(exception: Exception) {
        _error.value = exception.message ?: "An unexpected error occurred"
    }

    private fun clearListeners() {
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()
    }

    override fun onCleared() {
        clearListeners()
        super.onCleared()
    }
}
