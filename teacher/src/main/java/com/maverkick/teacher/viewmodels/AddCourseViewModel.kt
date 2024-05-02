package com.maverkick.teacher.viewmodels

import androidx.lifecycle.ViewModel
import com.maverkick.data.models.VideoCourse
import com.maverkick.data.repositories.CourseRepository
import com.maverkick.data.repositories.CourseStatisticsRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for the AddCourse Fragment
 * This viewModel class will be responsible for
 * the creation of the new course
 **/
@HiltViewModel
class AddCourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val statisticsRepository: CourseStatisticsRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _courseName = MutableStateFlow("")
    val courseName: StateFlow<String> = _courseName.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()


    fun updateCourseName(name: String) {
        _courseName.value = name
    }

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun addTag(tag: String): Boolean {
        val currentTags = _tags.value.toMutableList()
        return if (currentTags.size < 5) {
            currentTags.add(tag)
            _tags.value = currentTags
            true
        } else {
            false
        }
    }

    fun removeTag(tag: String) {
        val currentTags = _tags.value.toMutableList()
        currentTags.remove(tag)
        _tags.value = currentTags
    }

    fun submitCourse(onResult: (Boolean, String) -> Unit) {
        val courseName = _courseName.value
        val language = _selectedLanguage.value
        val tags = _tags.value

        if (courseName.isNotBlank() && language.isNotBlank() && tags.isNotEmpty()) {
            val teacher = sharedPrefManager.getTeacher()
            teacher?.let {
                val newVideoCourse = VideoCourse(
                    "",
                    courseName,
                    it.teacherId,
                    language,
                    "",
                    0,
                    tags,
                    Date(),
                    false
                )
                courseRepository.addCourse(newVideoCourse, { courseId ->
                    // Once the course is successfully created, initialize the corresponding CourseStatistics
                    statisticsRepository.addCourseStatistics(courseId,courseName, {
                        // If initializing the CourseStatistics is successful
                        onResult(true, courseId)
                    }, {
                        // If there was an error initializing the CourseStatistics
                        onResult(false, "Sorry, but we couldn't initialize course statistics, try again!")
                    })
                }, {
                    onResult(false, "Sorry, but we can't create the course, try it again!")
                })
            } ?: run {
                onResult(false, "Hey, we can't find your student id, something is wrong")
            }
        } else {
            onResult(false, "Please, fill in the course name,language and tags")
        }
    }
}
