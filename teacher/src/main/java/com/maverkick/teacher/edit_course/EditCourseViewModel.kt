package com.maverkick.teacher.edit_course

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverkick.data.models.VideoCourse
import com.maverkick.data.models.VideoLesson
import com.maverkick.data.repositories.VideoCourseRepository
import com.maverkick.data.repositories.VideoLessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Edit Course: add lessons, change poster and so on
 **/
@HiltViewModel
class EditCourseViewModel @Inject constructor(
    private val videoCourseRepository: VideoCourseRepository,
    private val videoLessonRepository: VideoLessonRepository
) : ViewModel() {

    private val _videoCourse = MutableLiveData<VideoCourse>()
    val videoCourse: LiveData<VideoCourse> = _videoCourse

    private val _lessons = MutableLiveData<List<VideoLesson>>()
    val lessons: LiveData<List<VideoLesson>> = _lessons

    val posterUri: MutableLiveData<Uri> = MutableLiveData()

    /** update the poster on the cloud when, teacher changed it */
    fun updatePoster(uri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        // Extract the courseId from the course object and use it for updating the poster.
        _videoCourse.value?.let { course ->
            videoCourseRepository.updatePoster(course.courseId, uri,
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
        val coursePoster = _videoCourse.value?.poster
        if (totalDurationMinutes < 1800) {
            onFailure(Exception("Total course duration is less than 30 minutes"))
            return
        }

        if (coursePoster == null || coursePoster.isBlank()) {
            onFailure(Exception("The course should have a poster set up for it"))
            return
        }
        videoCourseRepository.activateCourse(courseId, onSuccess, onFailure)
    }


    /** Fetch all of the information related to the course **/
    fun fetchCourse(courseId: String) {
        videoCourseRepository.getCourseById(courseId, { course ->
            _videoCourse.value = course

        }, {
        })
    }


    /** Fetch the lessons for a given course **/
    fun fetchLessons(courseId: String) {
        videoLessonRepository.getCourseLessons(courseId, { lessons ->
            _lessons.value = lessons
        }, {
            // Handle error: Show error message to the user
        })
    }

    /** Delete the course **/
    fun deleteCourse(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        videoCourseRepository.removeCourse(
            videoCourse.value!!.courseId,
            {
                onSuccess() // Successfully deleted the course
            },
            { exception ->
                onFailure(exception) // There was an error deleting the course
            }
        )
    }
}
