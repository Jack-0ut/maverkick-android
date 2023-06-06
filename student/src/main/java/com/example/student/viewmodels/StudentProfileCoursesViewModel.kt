package com.example.student.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.repositories.StudentRepository
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
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _currentCourses = MutableLiveData<List<Course>>()
    val currentCourses: LiveData<List<Course>> = _currentCourses

    init {
        fetchCourses()
    }

    /** A coroutine method that fetches the list of the courses that student is taking */
    private fun fetchCourses() {
        // Fetch the list of courses from the repository using a coroutine
        viewModelScope.launch {
            val studentResult = studentRepository.getCurrentStudent()
            studentResult.fold(
                onSuccess = { student ->
                    courseRepository.getStudentCourses(
                        student.studentId,
                        onSuccess = { courses ->
                            // Update the _courses LiveData with the fetched courses
                            _currentCourses.value = courses
                        },
                        onFailure = { error ->
                            // Handle the error case
                            Log.e("ProfileSettingsViewModel", "Error fetching student data: $error")
                        }
                    )
                },
                onFailure = { error ->
                    // Handle the error case
                    Log.e("ProfileSettingsViewModel", "Error fetching user data: $error")
                }
            )
        }
    }

}
