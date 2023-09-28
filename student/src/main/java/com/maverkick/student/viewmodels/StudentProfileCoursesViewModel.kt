package com.maverkick.student.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.maverkick.data.event_bus.CourseWithdrawnEvent
import com.maverkick.data.event_bus.EventBus
import com.maverkick.data.models.Course
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.data.repositories.*
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ProfileCoursesFragment
 * This ViewModel class fetches the list of courses
 * the student currently taking,stores and update it
 **/
@HiltViewModel
class StudentProfileCoursesViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager,
    private val videoCourseRepository: VideoCourseRepository,
    private val textCourseRepository: TextCourseRepository,
    private val studentRepository: StudentRepository,
    private val studentCourseRepository: StudentCourseRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository,
    private val dailyLearningPlanRepository: DailyLearningPlanRepository
) : ViewModel() {

    private val _currentVideoCourses = MutableLiveData<List<VideoCourse>>()
    private val _currentTextCourses = MutableLiveData<List<TextCourse>>()
    private val _currentCourses = MediatorLiveData<List<Course>>()

    val currentCourses: LiveData<List<Course>> = _currentCourses

    init {
        fetchVideoCourses()
        fetchTextCourses()

        // Add the sources to merge the video and text courses
        _currentCourses.addSource(_currentVideoCourses) { mergeCourses() }
        _currentCourses.addSource(_currentTextCourses) { mergeCourses() }
    }

    private fun mergeCourses() {
        val videoCourses = _currentVideoCourses.value ?: emptyList()
        val textCourses = _currentTextCourses.value ?: emptyList()
        _currentCourses.value = videoCourses + textCourses
    }

    private fun fetchVideoCourses() {
        viewModelScope.launch {
            val student = sharedPrefManager.getStudent()
            student?.let {
                studentRepository.getVideoCourses(it.studentId).let { result ->
                    if (result.isSuccess) {
                        val courseIds = result.getOrNull() ?: emptyList()

                        // Pass those courseIds to fetch the courses
                        videoCourseRepository.getVideoCoursesByIds(courseIds,
                            onSuccess = { videoCourses ->
                                _currentVideoCourses.value = videoCourses
                            },
                            onFailure = {}
                        )
                    }
                }
            } ?: run {
                // Handle the case where the student is null
            }
        }
    }

    private fun fetchTextCourses() {
        viewModelScope.launch {
            val student = sharedPrefManager.getStudent()
            student?.let {
                studentRepository.getTextCourses(it.studentId).let { result ->
                    if (result.isSuccess) {
                        val courseIds = result.getOrNull() ?: emptyList()

                        // Pass those courseIds to fetch the courses
                        textCourseRepository.getTextCoursesByIds(courseIds,
                            onSuccess = { textCourses ->
                                _currentTextCourses.value = textCourses
                            },
                            onFailure = {}
                        )
                    }
                }
            } ?: run {
                // Handle the case where the student is null
            }
        }
    }

    fun withdrawFromCourse(courseId: String, courseType: CourseType) {
        viewModelScope.launch {
            val savedStudent = sharedPrefManager.getStudent() ?: return@launch
            try {
                studentCourseRepository.withdrawStudent(savedStudent.studentId, courseId)
                studentRepository.removeCourseFromEnrolled(savedStudent.studentId, courseId, courseType)
                dailyLearningPlanRepository.updateOnCourseDrop(savedStudent.studentId, courseId, savedStudent.dailyStudyTimeMinutes * 60)

                val newEnrolledCourses = when (courseType) {
                    CourseType.VIDEO -> savedStudent.enrolledVideoCourses.filterNot { it == courseId }
                    CourseType.TEXT -> savedStudent.enrolledTextCourses.filterNot { it == courseId }
                }

                val updatedStudent = savedStudent.copy(
                    enrolledVideoCourses = if (courseType == CourseType.VIDEO) newEnrolledCourses else savedStudent.enrolledVideoCourses,
                    enrolledTextCourses = if (courseType == CourseType.TEXT) newEnrolledCourses else savedStudent.enrolledTextCourses
                )

                sharedPrefManager.saveStudent(updatedStudent)

                when (courseType) {
                    CourseType.VIDEO -> _currentVideoCourses.value = _currentVideoCourses.value?.filterNot { it.courseId == courseId }
                    CourseType.TEXT -> _currentTextCourses.value = _currentTextCourses.value?.filterNot { it.courseId == courseId }
                }

                if (courseType == CourseType.VIDEO) {
                    courseStatisticsRepository.incrementDropouts(
                        courseId,
                        onSuccess = { Log.d("Withdrawal", "Successfully incremented dropouts.") },
                        onFailure = { e -> Log.e("Withdrawal", "Failed to increment dropouts: ", e) }
                    )
                }
                // Emit the event after withdrawing
                EventBus.courseWithdrawnEvent.emit(CourseWithdrawnEvent(savedStudent.studentId, courseId, courseType))

            } catch (e: Exception) {
                Log.e("Withdrawal", "Failed to withdraw from course: ", e)
            }
        }
    }

}
