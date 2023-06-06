package com.example.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.repositories.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the AddCourse Fragment
 * This viewModel class will be responsible for
 * the creation of the new course
 **/
@HiltViewModel
class AddCourseViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {

    private val _courseName = MutableLiveData<String>()
    val courseName: LiveData<String> get() = _courseName

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    private val _selectedLanguage = MutableLiveData<String>()
    val selectedLanguage: LiveData<String> get() = _selectedLanguage

    fun submitCourse() {
        // Validate the data before submission
        if (_courseName.value.isNullOrBlank() ||  _tags.value.isNullOrEmpty() || _selectedLanguage.value.isNullOrBlank()) {
            //repository.addCourse()
        }

        // TODO: Implement your logic here to add a new course
    }
    fun updateCourseName(name: String) {
        _courseName.value = name
    }

    fun updateTags(tags: List<String>) {
        _tags.value = tags
    }

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
    }

    /** Method that takes the video and send it to the api to do the necessary transformation**/
    fun uploadLesson(){

    }
}
