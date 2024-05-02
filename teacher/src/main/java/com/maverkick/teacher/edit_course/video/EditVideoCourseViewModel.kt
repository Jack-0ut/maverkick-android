package com.maverkick.teacher.edit_course.video

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.maverkick.data.models.VideoCourse
import com.maverkick.data.models.VideoLesson
import com.maverkick.data.repositories.CourseRepository
import com.maverkick.data.repositories.VideoLessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Edit Course: add lessons, change poster and so on
 **/
@HiltViewModel
class EditVideoCourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val videoLessonRepository: VideoLessonRepository
) : ViewModel() {

    private val _videoCourse = MutableLiveData<VideoCourse>()
    val videoCourse: LiveData<VideoCourse> = _videoCourse

    private val _lessons = MutableLiveData<List<VideoLesson>>()
    val lessons: LiveData<List<VideoLesson>> = _lessons

    private val _errors = MutableLiveData<String>()
    val errors: LiveData<String> = _errors

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    val posterUri: MutableLiveData<Uri> = MutableLiveData()

    fun updatePoster(uri: Uri) {
        _videoCourse.value?.courseId?.let { courseId ->
            courseRepository.updatePoster(courseId, uri,
                { updatedUriString ->
                    posterUri.postValue(Uri.parse(updatedUriString))
                },
                { exception -> _errors.postValue(exception.message ?: "Unknown error occurred while updating poster") }
            )
        } ?: run { _errors.postValue("Course not available") }
    }

    fun publishCourse(courseId: String) {
        val totalDurationMinutes = _lessons.value?.sumOf { it.duration } ?: 0
        val coursePoster = _videoCourse.value?.poster

        when {
            totalDurationMinutes < 1800 -> _errors.postValue("Total course duration is less than 30 minutes")
            coursePoster == null || coursePoster.isBlank() -> _errors.postValue("The course should have a poster set up for it")
            else -> courseRepository.activateCourse(courseId,
                { /* Handle success if necessary */ },
                { exception -> _errors.postValue(exception.message ?: "Unknown error occurred while activating course") }
            )
        }
    }

    fun fetchCourse(courseId: String) {
        courseRepository.getCourseByIdRealTime(courseId,
            { course ->
                if (course is VideoCourse) {
                    _videoCourse.postValue(course)
                } else {
                    _errors.postValue("Invalid course type retrieved")
                }
            },
            { exception -> _errors.postValue(exception.message ?: "Unknown error occurred while fetching course") }
        )
    }

    fun fetchLessons(courseId: String) {
        videoLessonRepository.getCourseLessons(courseId,
            { lessons -> _lessons.postValue(lessons) },
            { exception -> _errors.postValue(exception.message ?: "Unknown error occurred while fetching lessons") }
        )
    }

    fun deleteCourse(onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        _videoCourse.value?.courseId?.let { courseId ->
            courseRepository.removeCourse(
                courseId,
                {
                    onSuccess("Course successfully deleted")
                },
                { exception ->
                    onFailure(exception.message ?: "Unknown error occurred while deleting course")
                }
            )
        } ?: run { onFailure("Course not available for deletion") }
    }
}


