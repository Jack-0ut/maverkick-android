package com.example.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.models.Course
import com.example.data.repositories.CourseRepository
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Teacher HomeFragment class
 * This ViewModel class would interact with our data source and
 * expose LiveData objects that Fragment can observe to update the UI.
 * So, basically it's fetching the list of courses from the database and
 * display it to the teacher, so he could edit or add new lessons to it
 **/
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val sharedPrefManager: SharedPrefManager
): ViewModel() {

    // LiveData object that the Fragmwent can observe to get the list of lessons
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> get() = _courses

    init {
        // getting the list of today's lessons
        //fetchCourses()
    }

}

