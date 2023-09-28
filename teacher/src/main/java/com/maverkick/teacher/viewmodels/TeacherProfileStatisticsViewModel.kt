package com.maverkick.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.models.VideoCourse
import com.maverkick.data.models.CourseStatistics
import com.maverkick.data.repositories.VideoCourseRepository
import com.maverkick.data.repositories.CourseStatisticsRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Teacher - Profile - Statistics Fragment,
 * which responsible for handling display of statistics of the courses
 **/
@HiltViewModel
class TeacherProfileStatisticsViewModel @Inject constructor(
    private val videoCourseRepository: VideoCourseRepository,
    private val statisticsRepository: CourseStatisticsRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _currentCourses = MutableLiveData<List<VideoCourse>>()
    val currentCourses: LiveData<List<VideoCourse>> = _currentCourses

    private val _courseStatistics = MutableLiveData<Map<String, CourseStatistics>>()
    val courseStatistics: LiveData<Map<String, CourseStatistics>> = _courseStatistics

    init {
        fetchCoursesStatistics()
    }

    private fun fetchCoursesStatistics() {
        viewModelScope.launch {
            // Fetch the current courses
            sharedPrefManager.getTeacher()?.let { teacher ->
                try {
                    val courses = videoCourseRepository.getTeacherCourses(teacher.teacherId)
                    _currentCourses.value = courses

                    // Now for each course fetch statistics and save them in a map
                    val statsMap = mutableMapOf<String, CourseStatistics>()

                    courses.forEach { course ->
                        val stats = statisticsRepository.getCourseStatistics(course.courseId)
                        stats?.let {
                            statsMap[course.courseId] = it
                        }
                    }

                    // Assign the stats map to _courseStatistics LiveData
                    _courseStatistics.value = statsMap
                } catch (exception: Exception) {
                    // Handle error here
                }
            } ?: run {
                // Handle case where teacher is null
            }
        }
    }
}
