package com.example.teacher.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.sharedpref.SharedPrefManager
import com.google.firebase.firestore.ListenerRegistration
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
): ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> get() = _courses

    private var listenerRegistrations = mutableListOf<ListenerRegistration>()

    init {
        fetchCourses()
    }

    /** Fetch the list of the courses for the current teacher **/
    fun fetchCourses() = viewModelScope.launch {
        // Clear any previous listeners
        listenerRegistrations.forEach { it.remove() }
        listenerRegistrations.clear()

        val teacherId = sharedPrefManager.getTeacher()?.teacherId
        teacherId?.let {
            try {
                val courses = courseRepository.getTeacherCourses(it)
                // Clear any previous listeners
                listenerRegistrations.forEach { it.remove() }
                listenerRegistrations.clear()

                // Start listening for changes to each course
                courses.forEach { course ->
                    val registration = courseRepository.getCourseById(course.courseId, { updatedCourse ->
                        // Replace the updated course in the _courses list
                        val updatedCourses = _courses.value.toMutableList()
                        val index = updatedCourses.indexOfFirst { it.courseId == updatedCourse?.courseId }
                        if (index != -1) {
                            updatedCourse?.let { updatedCourses[index] = it }
                            _courses.value = updatedCourses
                        }
                    }, {

                    })
                    listenerRegistrations.add(registration)
                }

                // Update the courses list
                _courses.value = courses
            } catch (exception: Exception) {
                // handle or log the exception
            }
        }
    }


    override fun onCleared() {
        // Remove all listeners when the ViewModel is cleared
        listenerRegistrations.forEach { it.remove() }
        super.onCleared()
    }
}
