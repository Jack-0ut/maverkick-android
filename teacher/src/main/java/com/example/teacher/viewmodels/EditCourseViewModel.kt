package com.example.teacher.viewmodels

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

    private val _lessons = MutableLiveData<List<Lesson>>() // Assuming Lesson is a data class representing a lesson
    val lessons: LiveData<List<Lesson>> = _lessons


    fun fetchCourse(courseId: String) {
        courseRepository.getCourseById(courseId, { course ->
            _course.value = course
        }, { exception ->
            // Handle error: Show error message to the user
        })
    }

    fun fetchLessons(courseId: String) {
        lessonRepository.getCourseLessons(courseId, { lessons ->
            _lessons.value = lessons
        }, { exception ->
            // Handle error: Show error message to the user
        })
    }
}
