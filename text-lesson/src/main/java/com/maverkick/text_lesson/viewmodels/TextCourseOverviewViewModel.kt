package com.maverkick.text_lesson.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.Student
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.TextLesson
import com.maverkick.data.repositories.StudentCourseRepository
import com.maverkick.data.repositories.StudentRepository
import com.maverkick.data.repositories.TextCourseRepository
import com.maverkick.data.repositories.TextLessonRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextCourseOverviewViewModel @Inject constructor(
    private val textCourseRepository: TextCourseRepository,
    private val textLessonRepository: TextLessonRepository,
    private val studentRepository: StudentRepository,
    private val studentCourseRepository: StudentCourseRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    // LiveData to observe the course information
    private val _course = MutableLiveData<TextCourse>()
    val course: LiveData<TextCourse> get() = _course

    // LiveData to observe the list of lessons
    private val _lessons = MutableLiveData<List<TextLesson>>()
    val lessons: LiveData<List<TextLesson>> get() = _lessons

    // LiveData to observe any errors
    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> get() = _error

    private val _enrollmentComplete = MutableLiveData<Boolean>()
    val enrollmentComplete: LiveData<Boolean> get() = _enrollmentComplete

    fun fetchCourseInformation(courseId: String) {
        // Fetch course information from the repository and update the LiveData
        textCourseRepository.getTextCourseById(courseId,
            onSuccess = { course ->
                _course.postValue(course)
            },
            onFailure = { exception ->
                _error.postValue(exception)
            })
    }

    fun fetchLessons(courseId: String) {
        // Fetch lessons from the repository and update the LiveData
        textLessonRepository.getTextCourseLessons(courseId,
            onSuccess = { lessonsList ->
                _lessons.postValue(lessonsList)
            },
            onFailure = { exception ->
                _error.postValue(exception)
            })
    }

    /** Enroll student in a personalized text course **/
    fun enrollStudentInTextCourse(courseId: String) {
        val student = sharedPrefManager.getStudent() ?: return // Early exit if no student

        if (student.enrolledTextCourses.contains(courseId)) return // Early exit if already enrolled

        viewModelScope.launch {
            try {
                enrollInTextCourse(student, courseId)
                updateSharedPreferencesForTextCourse(student, courseId)
                _enrollmentComplete.postValue(true)
            } catch (e: Exception) {
                _enrollmentComplete.postValue(false)
            }
        }
    }

    private suspend fun enrollInTextCourse(student: Student, courseId: String) {
        studentRepository.addCourseToEnrolled(student.studentId, courseId, CourseType.TEXT)

        if (studentCourseRepository.isStudentEverBeenEnrolled(student.studentId, courseId)) {
            studentCourseRepository.reEnrollStudent(student.studentId, courseId)
        } else {
            studentCourseRepository.enrollStudent(student.studentId, courseId, CourseType.TEXT)
            studentCourseRepository.initStudentCourseProgress(student.studentId, courseId, CourseType.TEXT)
        }
    }

    private fun updateSharedPreferencesForTextCourse(student: Student, courseId: String) {
        val updatedTextCourses = student.enrolledTextCourses.toMutableSet().apply { add(courseId) }
        student.enrolledTextCourses = updatedTextCourses.toList()
        sharedPrefManager.saveStudent(student)
    }


    fun resetEnrollmentCompleteFlag() {
        _enrollmentComplete.value = false
    }

}
