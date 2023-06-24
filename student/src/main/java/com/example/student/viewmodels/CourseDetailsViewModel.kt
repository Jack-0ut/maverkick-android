package com.example.student.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Course
import com.example.data.models.Lesson
import com.example.data.models.Teacher
import com.example.data.models.User
import com.example.data.repositories.CourseRepository
import com.example.data.repositories.LessonRepository
import com.example.data.repositories.TeacherRepository
import com.example.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailsViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val teacherRepository: TeacherRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _course = MutableLiveData<Course>()
    val course: LiveData<Course> = _course

    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> = _lessons

    private val _teacher = MutableLiveData<Teacher>()
    val teacher: LiveData<Teacher> = _teacher

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    /** Fetch course details **/
    fun fetchCourseDetails(courseId: String) {
        courseRepository.getCourseById(courseId, { course ->
            _course.value = course
            course?.let { fetchTeacherName(it.teacherId) }
        }, { exception ->
            // Handle error: Show error message to the user
        })
    }

    /** Fetch lessons for a given course **/
    fun fetchLessons(courseId: String) {
        lessonRepository.getCourseLessons(courseId, { lessons ->
            _lessons.value= lessons
        }, { exception ->
            // Handle error: Show error message to the user
        })
    }

    /** Fetch teacher name for a given teacherId **/
    private fun fetchTeacherName(teacherId: String) {
        viewModelScope.launch {
            try {
                val result = teacherRepository.getTeacherById(teacherId)
                if (result.isSuccess) {
                    _teacher.value = result.getOrNull()
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
                userRepository.getUserById(userId, { user ->
                    user?.let {
                        _user.value = it
                    }
                }, { exception ->
                    // Handle error: Show error message to the user
                })
            } catch (e: Exception) {
                // Handle error: Show error message to the user
            }
        }
    }

}
