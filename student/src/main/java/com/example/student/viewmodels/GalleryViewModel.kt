package com.example.student.viewmodels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel for the GalleryFragment
 * This viewModel class will be responsible for
 * the displaying the search results for course searching and caching the results
 **/
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    // Mutable LiveData for the list of courses
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> get() = _courses

    init {
        fetchCourses()
    }

    /** Fetch all the courses **/
    private fun fetchCourses() {
        // Launch a coroutine in the ViewModel's scope
        viewModelScope.launch {
            runCatching {
                // Fetch the list of courses from the repository
                courseRepository.getAllCourses()
            }.onSuccess { fetchedCourses ->
                // Update the _courses LiveData with the fetched courses
                _courses.value = fetchedCourses
            }.onFailure { exception ->
                // Handle the exception here
                Log.e(TAG, "Failed to fetch courses: ", exception)
            }
        }
    }
}
