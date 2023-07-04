package com.example.student.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ProfileCoursesFragment
 * This ViewModel class fetches the list of courses
 * the student currently taking,stores and update it
 **/
@HiltViewModel
class StudentProfileCoursesViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _currentCourses = MutableLiveData<List<Course>>()
    val currentCourses: LiveData<List<Course>> = _currentCourses

    init {
        fetchCourses()
    }

    /** A coroutine method that fetches the list of the courses that student is taking */
    private fun fetchCourses() {
        viewModelScope.launch {
            // Get the current user from shared preferences
            val student = sharedPrefManager.getStudent()
            student?.let {
                courseRepository.getStudentCourses(
                    it.studentId,
                    onSuccess = { courses ->
                        // Update the _courses LiveData with the fetched courses
                        _currentCourses.value = courses

                        // Log the fetched courses
                        Log.d("StudentProfileCoursesViewModel", "Fetched ${courses.size} courses: $courses")
                    },
                    onFailure = { error ->
                        // Handle the error case and log the error
                        Log.e("StudentProfileCoursesViewModel", "Error fetching courses: $error")
                    }
                )
            } ?: run {
                // Handle the case where there is no user in shared preferences and log the case
                Log.e("StudentProfileCoursesViewModel", "No user found in shared preferences")
            }
        }
    }

}
