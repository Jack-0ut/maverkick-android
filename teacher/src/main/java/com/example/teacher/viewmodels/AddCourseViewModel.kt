package com.example.teacher.viewmodels

import androidx.lifecycle.ViewModel
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.sharedpref.SharedPrefManager
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

        // if teacher filled out all of the fields, create the course
        if (courseName.isNotBlank() && language.isNotBlank() && tags.isNotEmpty()) {
            // get the id of the current teacher
            val teacher = sharedPrefManager.getTeacher()

            teacher?.let {
                val newCourse = Course(
                    courseId = "", // It will be replaced in the Firestore
                    courseName = courseName,
                    teacherId = it.teacherId, // You should implement this function
                    language = language,
                    poster = "", // It will be updated later
                    tags = tags,
                    creationDate = Date() // Current date
                )
                courseRepository.addCourse(newCourse, { courseId ->
                    // Handle success: You have a new course with courseId
                    onResult(true, courseId) // Call the callback with success status and courseId
                }, {
                    // Handle error: Show error message to the user
                    onResult(false, "Sorry, but we can't create the course, try it again!")
                })
            } ?: run {
                // Handle case where student is null
                onResult(false, "Hey, we can't find your student id, what's the matter?")
            }
        } else {
            // Handle case where course name or language is not filled in
            onResult(false, "Please, fill in the course name,language and tags")
        }
    }
}
