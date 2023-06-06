package com.example.teacher.viewmodels

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


}
