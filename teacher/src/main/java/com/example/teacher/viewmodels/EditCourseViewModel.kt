package com.example.teacher.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.models.Course
import com.example.data.models.Lesson
import com.example.data.repositories.CourseRepository
import com.example.data.repositories.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Edit Course: add lessons, change poster and so on
 **/
@HiltViewModel
class EditCourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val _course = MutableLiveData<Course>()
    val course: LiveData<Course> = _course

    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons

    val posterUri: MutableLiveData<Uri> = MutableLiveData()

    /** update the poster on the cloud when, teacher changed it */
    fun updatePoster(uri: Uri){
        // Extract the courseId from the course object and use it for updating the poster.
        _course.value?.let { course ->
            courseRepository.updatePoster(course.courseId, uri)
            // Also update the posterUri LiveData
            posterUri.value = uri
        } ?: run {
            // Handle error: Show error message to the user
        }
    }

    /** Fetch all of the information related to the course **/
    fun fetchCourse(courseId: String) {
        courseRepository.getCourseById(courseId, { course ->
            _course.value = course
        }, { exception ->
            // Handle error: Show error message to the user
        })
    }

    /** Fetch the lessons for a given course **/
    fun fetchLessons(courseId: String) {
        lessonRepository.getCourseLessons(courseId, { lessons ->
            _lessons.value = lessons
        }, { exception ->
            // Handle error: Show error message to the user
        })
    }
}
