package com.example.teacher.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
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

    // LiveData object that the Fragment can observe to get the list of courses
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> get() = _courses

    private val courseAddedFlow = MutableSharedFlow<Unit>()

    init {
        fetchCourses()

        viewModelScope.launch {
            courseAddedFlow.collect {
                fetchCourses()
            }
        }
    }

    /** Fetch the list of the courses for the current teacher **/
    private fun fetchCourses() {
        val teacherId = sharedPrefManager.getTeacher()?.teacherId
        teacherId?.let {
            courseRepository.getTeacherCourses(it, { courses ->
                _courses.value = courses
            }, { exception ->
                // Handle error: Show error message to the user
            })
        }
    }
}
