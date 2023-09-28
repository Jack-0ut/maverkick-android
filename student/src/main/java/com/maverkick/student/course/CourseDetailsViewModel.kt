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

/**
 * ViewModel for fetching the necessary data from different repositories
 * to display it in the CourseDetailsActivity. Also responsible for enrollment,
 * re-enrollment of the Student and how to track that/
 **/
@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    private val videoCourseRepository: VideoCourseRepository,
    private val videoLessonRepository: VideoLessonRepository,
    private val teacherRepository: TeacherRepository,
    private val userRepository: UserRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val studentCourseRepository: StudentCourseRepository,
    private val studentRepository:StudentRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository,
    private val dailyLearningPlanRepository: DailyLearningPlanRepository
) : ViewModel() {

    private val _course = MutableLiveData<VideoCourse>()
    val videoCourse: LiveData<VideoCourse> = _course

    private val _lessons = MutableLiveData<List<VideoLesson>>()
    val lessons: LiveData<List<VideoLesson>> = _lessons

    private val _teacher = MutableLiveData<Teacher>()
    val teacher: LiveData<Teacher> = _teacher

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _enrollmentComplete = MutableLiveData<Boolean>()
    val enrollmentComplete: LiveData<Boolean> get() = _enrollmentComplete

    private val _isAlreadyEnrolled = MutableLiveData<Boolean>()
    val isAlreadyEnrolled: LiveData<Boolean> get() = _isAlreadyEnrolled

    /** Fetch course details **/
    fun fetchCourseDetails(courseId: String) {
        checkEnrollmentStatus(courseId)
        videoCourseRepository.getCourseById(courseId, { course ->
            _course.value = course
            course?.let { fetchTeacherData(it.teacherId) }
        }, {
            // Handle error: Show error message to the user
        })
    }

    /** Fetch lessons for a given course **/
    fun fetchLessons(courseId: String) {
        videoLessonRepository.getCourseLessons(courseId, { lessons ->
            _lessons.value= lessons
        }, {

        })
    }

    /** Fetch teacher name for a given teacherId **/
    private fun fetchTeacherData(teacherId: String) {
        viewModelScope.launch {
            try {
                val result = teacherRepository.getTeacherById(teacherId)
                if (result.isSuccess) {
                    _teacher.value = result.getOrNull()
                    // Fetch the profile of the user who is a teacher
                    fetchUserProfile(teacherId)
                } else {
                    // Handle error: Show error message to the user
                }
            } catch (e: Exception) {
                // Handle error: Show error message to the user
            }
        }
    }

    /** Fetch user profile for a given userId **/
    private fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val fetchedUser = userRepository.getUserById(userId)
                fetchedUser?.let {
                    _user.value = it
                }
            } catch (e: Exception) {
                // Handle error: Show error message to the user
            }
        }
    }

    /** Check if the student is already enrolled in the course **/
    private fun checkEnrollmentStatus(courseId: String) {
        val student = sharedPrefManager.getStudent()
        student?.let {
            _isAlreadyEnrolled.value = it.enrolledVideoCourses.contains(courseId)
        }
    }
    /** Enroll student in the course **/
    fun enrollStudent(courseId: String) {
        val student = sharedPrefManager.getStudent() ?: return // Early exit if no student

        if (student.enrolledVideoCourses.contains(courseId)) return // Early exit if already enrolled

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
        studentRepository.addCourseToEnrolled(student.studentId, courseId, CourseType.VIDEO)

        if (studentCourseRepository.isStudentEverBeenEnrolled(student.studentId, courseId)) {
            studentCourseRepository.reEnrollStudent(student.studentId, courseId)
        } else {
            studentCourseRepository.enrollStudent(student.studentId, courseId, CourseType.VIDEO)
            studentCourseRepository.initStudentCourseProgress(student.studentId, courseId, CourseType.VIDEO)
        }
    }

    private suspend fun updateDailyLearningPlan(student: Student, courseId: String) {
        dailyLearningPlanRepository.updateOnCourseEnrollment(student.studentId, courseId, CourseType.VIDEO, student.dailyStudyTimeMinutes)
    }

    private fun updateSharedPreferencesAndStatistics(student: Student, courseId: String) {
        val updatedCourses = student.enrolledVideoCourses.toMutableSet().apply { add(courseId) }
        student.enrolledVideoCourses = updatedCourses.toList()
        sharedPrefManager.saveStudent(student)

        courseStatisticsRepository.incrementEnrollments(courseId,
            onSuccess = {},
            onFailure = {
                _enrollmentComplete.postValue(false) // Notify if there's a failure in updating statistics
            }
        )
    }

    fun resetEnrollmentCompleteFlag() {
        _enrollmentComplete.value = false
    }
}
