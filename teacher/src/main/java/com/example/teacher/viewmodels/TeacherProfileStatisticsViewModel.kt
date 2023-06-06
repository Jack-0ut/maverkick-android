package com.example.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.repositories.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Teacher - Profile - Statistics Fragment,
 * which responsible for handling display of statistics of the courses
 **/
@HiltViewModel
class TeacherProfileStatisticsViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _currentCourses = MutableLiveData<List<Course>>()
    val currentCourses: LiveData<List<Course>> = _currentCourses

    init {
        fetchCoursesStatistics()
    }

    /** A coroutine method that fetches the list of the courses that student is taking */
    private fun fetchCoursesStatistics() {
        // TODO Fetch the statistics for every course for given teacher id
    }
}
