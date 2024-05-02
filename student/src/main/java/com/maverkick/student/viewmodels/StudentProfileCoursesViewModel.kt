package com.maverkick.student.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.maverkick.data.models.Course
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.PersonalizedTextCourse
import com.maverkick.data.models.Student
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
    private val courseRepository: CourseRepository,
    private val personalizedTextCourseRepository: PersonalizedTextCourseRepository,
    private val studentRepository: StudentRepository,
    private val studentCourseRepository: StudentCourseRepository,
    private val dailyLearningPlanRepository: DailyLearningPlanRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository
) : ViewModel() {

    private val _generalCourses = MutableLiveData<List<Course>>()
    private val _personalizedTextCourses = MutableLiveData<List<PersonalizedTextCourse>>()

    private val _allEnrolledCourses = MediatorLiveData<List<Course>>()
    val allEnrolledCourses: LiveData<List<Course>> = _allEnrolledCourses

    private val _allFinishedCourses = MutableLiveData<List<Course>>()
    val allFinishedCourses: LiveData<List<Course>> = _allFinishedCourses

    // LiveData for reporting the completion status of withdrawal
    private val _withdrawalComplete = MutableLiveData<Boolean>()
    val withdrawalComplete: LiveData<Boolean> = _withdrawalComplete

    // LiveData for reporting errors
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        _allEnrolledCourses.addSource(_generalCourses) { mergeCourses() }
        _allEnrolledCourses.addSource(_personalizedTextCourses) { mergeCourses() }

        fetchGeneralCourses()
        fetchPersonalizedTextCourses()
        fetchFinishedCourses()
    }

    private fun mergeCourses() {
        val generalCourses = _generalCourses.value ?: emptyList()
        val personalizedTextCourses = _personalizedTextCourses.value ?: emptyList()
        _allEnrolledCourses.value = generalCourses + personalizedTextCourses
    }

    private fun fetchGeneralCourses() {
        viewModelScope.launch {
            val student = sharedPrefManager.getStudent()
            student?.let {
                studentRepository.getCourses(it.studentId).let { result ->
                    if (result.isSuccess) {
                        val courseIds = result.getOrNull() ?: emptyList()
                        courseRepository.getCoursesByIds(courseIds,
                            onSuccess = { courses ->
                                _generalCourses.value = courses
                            },
                            onFailure = { /* Handle error here */ }
                        )
                    }
                }
            } ?: run { /* Handle error here */ }
        }
    }

    private fun fetchPersonalizedTextCourses() {
        viewModelScope.launch {
            val student = sharedPrefManager.getStudent()
            student?.let {
                studentRepository.getGeneratedTextCourses(it.studentId).let { result ->
                    if (result.isSuccess) {
                        val courseIds = result.getOrNull() ?: emptyList()
                        personalizedTextCourseRepository.getGeneratedTextCoursesByIds(courseIds,
                            onSuccess = { textCourses ->
                                _personalizedTextCourses.value = textCourses
                            },
                            onFailure = { /* Handle error here */ }
                        )
                    }
                }
            } ?: run { /* Handle error here */ }
        }
    }

    private fun fetchFinishedCourses() {
        viewModelScope.launch {
            val student = sharedPrefManager.getStudent()
            student?.let {
                studentRepository.getFinishedCourses(it.studentId).let { result ->
                    if (result.isSuccess) {
                        val finishedCourseIds = result.getOrNull() ?: emptyList()
                        courseRepository.getCoursesByIds(finishedCourseIds,
                            onSuccess = { courses ->
                                _allFinishedCourses.value = courses
                            },
                            onFailure = { /* Handle error here */ }
                        )
                    }
                }
            } ?: run { /* Handle error here */ }
        }
    }

    private fun withdrawAndUpdateStatistics(courseId: String, courseType: CourseType) {
        viewModelScope.launch {
            val savedStudent = sharedPrefManager.getStudent() ?: return@launch
            try {
                studentCourseRepository.withdrawStudent(savedStudent.studentId, courseId)
                studentRepository.removeCourseFromEnrolled(savedStudent.studentId, courseId, courseType)
                dailyLearningPlanRepository.updateOnCourseDrop(savedStudent.studentId, courseId, savedStudent.dailyStudyTimeMinutes * 60)

                val newEnrolledCourses = when (courseType) {
                    CourseType.VIDEO, CourseType.TEXT -> savedStudent.enrolledCourses.filterNot { it == courseId }
                    CourseType.TEXT_PERSONALIZED -> savedStudent.enrolledGeneratedCourses.filterNot { it == courseId }
                }

                val updatedStudent = savedStudent.copy(
                    enrolledCourses = (if (courseType == CourseType.VIDEO || courseType == CourseType.TEXT) newEnrolledCourses else savedStudent.enrolledCourses),
                    enrolledGeneratedCourses = (if (courseType == CourseType.TEXT_PERSONALIZED) newEnrolledCourses else savedStudent.enrolledGeneratedCourses)
                )

                sharedPrefManager.saveStudent(updatedStudent)

                when (courseType) {
                    CourseType.VIDEO,CourseType.TEXT  -> _generalCourses.value = _generalCourses.value?.filterNot { it.courseId == courseId }
                    CourseType.TEXT_PERSONALIZED -> _personalizedTextCourses.value = _personalizedTextCourses.value?.filterNot { it.courseId == courseId }
                    else -> {}
                }

                if (courseType == CourseType.VIDEO || courseType == CourseType.TEXT) {
                    courseStatisticsRepository.incrementDropouts(
                        courseId,
                        onSuccess = { Log.d("Withdrawal", "Successfully incremented dropouts.") },
                        onFailure = { e -> Log.e("Withdrawal", "Failed to increment dropouts: ", e) }
                    )
                }
            } catch (e: Exception) { }
        }
    }

    fun withdrawFromCourse(courseId: String, courseType: CourseType) {
        val student = sharedPrefManager.getStudent() ?: return
        viewModelScope.launch {
            try {
                withdrawAndUpdateStatistics(courseId, courseType)
                updateDailyLearningPlanOnWithdrawal(student, courseId)
                _withdrawalComplete.postValue(true)
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "An unknown error occurred during withdrawal.")
                _withdrawalComplete.postValue(false)
            }
        }
    }

    /** We drop from the course, hence we should update the daily learning plan **/
    private suspend fun updateDailyLearningPlanOnWithdrawal(student: Student, courseId: String) {
        dailyLearningPlanRepository.updateOnCourseDrop(student.studentId, courseId, student.dailyStudyTimeMinutes * 60)
    }

    fun resetWithdrawalCompleteFlag() {
        _withdrawalComplete.value = false
    }
}
