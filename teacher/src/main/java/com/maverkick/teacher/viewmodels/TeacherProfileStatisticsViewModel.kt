package com.maverkick.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.models.Course
import com.maverkick.data.models.CourseStatistics
import com.maverkick.data.repositories.CourseRepository
import com.maverkick.data.repositories.CourseStatisticsRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class TeacherProfileStatisticsViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val statisticsRepository: CourseStatisticsRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _currentCourses = MutableLiveData<List<Course>>()

    private val _courseStatistics = MutableLiveData<Map<String, CourseStatistics>>()
    val courseStatistics: LiveData<Map<String, CourseStatistics>> = _courseStatistics

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        fetchCoursesStatistics()
    }

    private fun fetchCoursesStatistics() {
        viewModelScope.launch {
            // Fetch the current courses
            val teacher = sharedPrefManager.getTeacher()

            if(teacher == null) {
                _error.postValue("Error: Teacher not found")
                return@launch
            }

            try {
                val courses = courseRepository.getTeacherCourses(teacher.teacherId)
                _currentCourses.postValue(courses)

                // Now for each course fetch statistics and save them in a map
                val statsMap = mutableMapOf<String, CourseStatistics>()

                courses.forEach { course ->
                    val stats = statisticsRepository.getCourseStatistics(course.courseId)
                    stats?.let {
                        statsMap[course.courseId] = it
                    }
                }

                // Assign the stats map to _courseStatistics LiveData
                _courseStatistics.postValue(statsMap)
            } catch (exception: Exception) {
                _error.postValue("Error fetching data: ${exception.message}")
            }
        }
    }
}
