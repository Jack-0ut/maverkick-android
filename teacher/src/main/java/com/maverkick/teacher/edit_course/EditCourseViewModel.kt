package com.maverkick.teacher.edit_course

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverkick.data.models.Course
import com.maverkick.data.models.Lesson
import com.maverkick.data.repositories.CourseRepository
import com.maverkick.data.repositories.LessonRepository
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
    fun updatePoster(uri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        // Extract the courseId from the course object and use it for updating the poster.
        _course.value?.let { course ->
            courseRepository.updatePoster(course.courseId, uri,
                {
                    // Also update the posterUri LiveData
                    posterUri.value = uri
                    onSuccess()
                },
                { exception ->
                    onFailure(exception)
                }
            )
        }
    }

    /** Publish the course **/
    fun publishCourse(courseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val totalDurationMinutes = _lessons.value?.sumOf { it.duration } ?: 0
        val coursePoster = _course.value?.poster
        if (totalDurationMinutes < 1800) {
            onFailure(Exception("Total course duration is less than 30 minutes"))
            return
        }

        if (coursePoster == null || coursePoster.isBlank()) {
            onFailure(Exception("The course should have a poster set up for it"))
            return
        }
        courseRepository.activateCourse(courseId, onSuccess, onFailure)
    }


    /** Fetch all of the information related to the course **/
    fun fetchCourse(courseId: String) {
        courseRepository.getCourseById(courseId, { course ->
            _course.value = course

            // Log the course details
            Log.d("EditCourseViewModel", "Fetched course: $course")
        }, { exception ->
            // Handle error: Show error message to the user
            Log.e("EditCourseViewModel", "Error fetching course: ${exception.message}")
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

    /** Delete the course **/
    fun deleteCourse(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        courseRepository.removeCourse(
            course.value!!.courseId,
            {
                onSuccess() // Successfully deleted the course
            },
            { exception ->
                onFailure(exception) // There was an error deleting the course
            }
        )
    }
}
