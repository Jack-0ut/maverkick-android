package com.example.student.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.repositories.CourseStatisticsRepository
import com.example.data.repositories.StudentCourseRepository
import com.example.data.repositories.StudentRepository
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
    private val courseRepository: CourseRepository,
    private val studentRepository: StudentRepository,
    private val studentCourseRepository: StudentCourseRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository
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
                    },
                    onFailure = { error ->
                    }
                )
            } ?: run {
            }
        }
    }

    /** A coroutine method that withdraws the student from a course */
    fun withdrawFromCourse(courseId: String) {
        viewModelScope.launch {
            val student = sharedPrefManager.getStudent()
            student?.let { student ->
                try {
                    // Call the StudentCourseRepository to withdraw the student from the course
                    studentCourseRepository.withdrawStudent(student.studentId, courseId)
                    studentRepository.removeCourseFromEnrolled(student.studentId, courseId)

                    // Create a new list without the course ID
                    val newEnrolledCourses = student.enrolledCourses.filterNot { it == courseId }

                    // Update the Student object and save it back to shared preferences
                    val updatedStudent = student.copy(enrolledCourses = newEnrolledCourses)
                    sharedPrefManager.saveStudent(updatedStudent)

                    // Remove the course from the current LiveData
                    _currentCourses.value = _currentCourses.value?.filterNot { it.courseId == courseId }

                    // Update courseStatistics for the withdrawn course
                    courseStatisticsRepository.incrementDropouts(courseId,
                        onSuccess = {
                            // Log success or handle it
                            Log.d("Withdrawal", "Successfully incremented dropouts.")
                        },
                        onFailure = { e ->
                            // Log the error or handle it
                            Log.e("Withdrawal", "Failed to increment dropouts: ", e)
                        }
                    )
                } catch (e: Exception) {
                    // Log the error or handle it
                    Log.e("Withdrawal", "Failed to withdraw from course: ", e)
                }
            }
        }
    }
}
