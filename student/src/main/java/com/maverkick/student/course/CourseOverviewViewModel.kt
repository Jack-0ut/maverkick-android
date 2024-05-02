package com.maverkick.student.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.models.*
import com.maverkick.data.repositories.*
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseOverviewViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val videoLessonRepository: VideoLessonRepository,
    private val textLessonRepository: TextLessonRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val studentCourseRepository: StudentCourseRepository,
    private val studentRepository: StudentRepository,
    private val dailyLearningPlanRepository: DailyLearningPlanRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository
) : ViewModel() {

    private val _course = MutableLiveData<Course>()
    val courseDetails: LiveData<Course> get() = _course

    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> get() = _lessons

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _enrollmentComplete = MutableLiveData<Boolean>()
    val enrollmentComplete: LiveData<Boolean> get() = _enrollmentComplete

    private val _isAlreadyEnrolled = MutableLiveData<Boolean>()
    val isAlreadyEnrolled: LiveData<Boolean> get() = _isAlreadyEnrolled

    /** Fetch course details **/
    fun fetchCourseDetails(courseId: String) {
        checkEnrollmentStatus(courseId)
        courseRepository.getCourseById(courseId, { course ->
            _course.value = course
        }, { e ->
            _errorMessage.postValue(e.message ?: "Unknown error")
        })
    }

    /** Fetch lessons for a given course **/
    fun fetchLessons(courseId: String, courseType: CourseType) {
        when (courseType) {
            CourseType.VIDEO -> {
                fetchVideoLessons(courseId)
            }
            CourseType.TEXT -> {
                fetchTextLessons(courseId)
            }
            else -> {
                _errorMessage.postValue("Unsupported course type: $courseType")
            }
        }
    }

    private fun fetchVideoLessons(courseId: String) {
        videoLessonRepository.getCourseLessons(courseId, { lessons ->
            _lessons.value = lessons
        }, { e ->
            _errorMessage.postValue(e.message ?: "Error fetching video lessons for course with ID: $courseId")
        })
    }

    private fun fetchTextLessons(courseId: String) {
        textLessonRepository.getTextLessons(courseId, CourseType.TEXT, { lessons ->
            _lessons.value = lessons
        }, { e ->
            _errorMessage.postValue(e.message ?: "Error fetching text lessons for course with ID: $courseId")
        })
    }

    /** Check if the student is already enrolled in the course **/
    private fun checkEnrollmentStatus(courseId: String) {
        val student = sharedPrefManager.getStudent()
        student?.let {
            _isAlreadyEnrolled.value = it.enrolledCourses.contains(courseId)
        }
    }
    /** Enroll student in the course **/
    fun enrollStudent(courseId: String) {
        val student = sharedPrefManager.getStudent() ?: return
        if (student.enrolledCourses.contains(courseId)) return

        viewModelScope.launch {
            try {
                enrollInCourse(student, courseId)
                updateDailyLearningPlan(student, courseId)
                updateSharedPreferencesAndStatistics(student, courseId)
                _enrollmentComplete.postValue(true)
            } catch (e: Exception) {
                _enrollmentComplete.postValue(false)
            }
        }
    }

    private suspend fun enrollInCourse(student: Student, courseId: String) {
        val course = _course.value
            ?: throw IllegalStateException("Course must not be null")

        val courseType = when(course) {
            is VideoCourse -> CourseType.VIDEO
            is TextCourse -> CourseType.TEXT
            else -> throw IllegalArgumentException("Unsupported course type")
        }

        studentRepository.addCourseToEnrolled(student.studentId, courseId, courseType)

        if (studentCourseRepository.isStudentEverBeenEnrolled(student.studentId, courseId)) {
            studentCourseRepository.reEnrollStudent(student.studentId, courseId)
        } else {
            studentCourseRepository.enrollStudent(student.studentId, courseId, courseType)
            studentCourseRepository.initStudentCourseProgress(student.studentId, courseId, courseType)
        }
    }

    private suspend fun updateDailyLearningPlan(student: Student, courseId: String) {
        val course = _course.value
            ?: throw IllegalStateException("Course must not be null")

        val courseType = when(course) {
            is VideoCourse -> CourseType.VIDEO
            is TextCourse -> CourseType.TEXT
            else -> throw IllegalArgumentException("Unsupported course type")
        }
        dailyLearningPlanRepository.updateOnCourseEnrollment(student.studentId, courseId, courseType, student.dailyStudyTimeMinutes)
    }

    private fun updateSharedPreferencesAndStatistics(student: Student, courseId: String) {
        val updatedCourses = student.enrolledCourses.toMutableSet().apply { add(courseId) }
        student.enrolledCourses = updatedCourses.toList()
        sharedPrefManager.saveStudent(student)

        courseStatisticsRepository.incrementEnrollments(courseId,
            onSuccess = {},
            onFailure = { _enrollmentComplete.postValue(false) }
        )
    }

    fun resetEnrollmentCompleteFlag() {
        _enrollmentComplete.value = false
    }
}
