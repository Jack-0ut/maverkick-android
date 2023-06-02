package com.example.student.viewmodels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
 * ViewModel for the GalleryFragment
 * This viewModel class will be responsible for
 * the displaying the search results for course searching
 * @param repository - CourseRepository, from which we will be fetching the courses
 **/
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {

    // MutableLiveData that will store the list of courses
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    // MutableLiveData to hold the search query
    val searchQuery = MutableLiveData<String>()

    // Function to search for courses based on a query.
    fun searchCourses(query: String) {
        // Fetch the list of courses from the repository based on the query
        repository.searchCourses(query,
            onSuccess = { fetchedCourses ->
                // Update the _courses LiveData with the fetched courses
                _courses.value = fetchedCourses
            },
            onFailure = { exception ->
                // Handle the exception here
                Log.e(TAG, "Failed to fetch courses: ", exception)
            }
        )
    }
}
