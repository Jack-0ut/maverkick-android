package com.example.student.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.models.Lesson
import com.example.data.models.Teacher
import com.example.data.models.User
import com.example.data.repositories.*
import com.example.data.sharedpref.SharedPrefManager
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
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val teacherRepository: TeacherRepository,
    private val userRepository: UserRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val studentCourseRepository: StudentCourseRepository,
    private val studentRepository:StudentRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository
) : ViewModel() {

    private val _course = MutableLiveData<Course>()
    val course: LiveData<Course> = _course

    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons

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
        courseRepository.getCourseById(courseId, { course ->
            _course.value = course
            course?.let { fetchTeacherData(it.teacherId) }
        }, {
            // Handle error: Show error message to the user
        })
    }

    /** Fetch lessons for a given course **/
    fun fetchLessons(courseId: String) {
        lessonRepository.getCourseLessons(courseId, { lessons ->
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
            _isAlreadyEnrolled.value = it.enrolledCourses.contains(courseId)
        }
    }
    /** Enroll student in the course **/
    fun enrollStudent(courseId: String) {
        val student = sharedPrefManager.getStudent()
        student?.let {
            // check if the course is already enrolled
            if (!it.enrolledCourses.contains(courseId)) {
                viewModelScope.launch {
                    try {
                        studentRepository.addCourseToEnrolled(it.studentId, courseId)

                        val isStudentEverBeenEnrolled = studentCourseRepository.isStudentEverBeenEnrolled(it.studentId, courseId)
                        if (!isStudentEverBeenEnrolled) {
                            studentCourseRepository.enrollStudent(it.studentId, courseId)
                            studentCourseRepository.initStudentCourseProgress(it.studentId, courseId)
                        } else {
                            studentCourseRepository.reEnrollStudent(it.studentId, courseId)
                        }

                        // Add the newly enrolled course to the Student object and save it back to SharedPreferences
                        val updatedCourses = it.enrolledCourses.toMutableSet()
                        updatedCourses.add(courseId)
                        it.enrolledCourses = updatedCourses.toList()
                        sharedPrefManager.saveStudent(it)

                        // Update courseStatistics for the enrolled course
                        courseStatisticsRepository.incrementEnrollments(courseId,
                            onSuccess = {
                                _enrollmentComplete.postValue(true)
                            },
                            onFailure = {
                                _enrollmentComplete.postValue(false)
                            }
                        )
                    } catch (e: Exception) {
                        _enrollmentComplete.postValue(false)
                    }
                }
            }
        }
    }
    
    fun resetEnrollmentCompleteFlag() {
        _enrollmentComplete.value = false
    }
}
