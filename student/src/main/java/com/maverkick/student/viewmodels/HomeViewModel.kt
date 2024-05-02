package com.maverkick.student.viewmodels

import androidx.lifecycle.*
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.DailyLearningPlan
import com.maverkick.data.repositories.*
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel for the HomeFragment.
 * This ViewModel interacts with the data sources,updates the progress of the student
 * and exposes LiveData objects that Fragment can observe to update the UI.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dailyLearningPlanRepository: DailyLearningPlanRepository,
    private val studentCourseRepository: StudentCourseRepository,
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
    private val courseStatisticsRepository: CourseStatisticsRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val student = sharedPrefManager.getStudent()

    private val _dailyLearningPlan = MutableLiveData<DailyLearningPlan>()
    val dailyLearningPlan: LiveData<DailyLearningPlan> get() = _dailyLearningPlan

    private val _currentLessonIndex = MutableLiveData(0)
    val currentLessonIndex: LiveData<Int> get() = _currentLessonIndex

    val isLastLesson: LiveData<Boolean> = dailyLearningPlan.map { plan ->
        _currentLessonIndex.value!! >= plan.lessons.size - 1
    }

    private val _navigateToCourseEnrollment = MutableLiveData<Boolean>()
    val navigateToCourseEnrollment: LiveData<Boolean> get() = _navigateToCourseEnrollment

    private val _bricksCollected = MutableLiveData<Int>(student?.bricksCollected)
    val bricksCollected: LiveData<Int> get() = _bricksCollected

    private val _showCourseFinished = MutableLiveData<Boolean>()
    val showCourseFinished: LiveData<Boolean> get() = _showCourseFinished

    init {
        if (student?.enrolledCourses?.isEmpty() == true) {
            _navigateToCourseEnrollment.postValue(true)
        } else {
            loadDailyLearningPlan()
        }
    }

    private fun loadDailyLearningPlan() {
        viewModelScope.launchSafe {
            val plan = dailyLearningPlanRepository.getDailyLearningPlanForStudent(
                student?.studentId ?: "",
                student?.dailyStudyTimeMinutes ?: 0
            )
            _dailyLearningPlan.value = plan
            _currentLessonIndex.value = plan.progress
        }
    }

    /** Whenever completed lessons is not yet in the finished, we update it **/
    fun handleLessonCompleted(lessonId: String, courseId: String) {
        viewModelScope.launch {
            val updatedLessonCount = updateStudentLearningProgress(lessonId, courseId)
            checkCourseCompletion(updatedLessonCount, courseId)
        }
    }

    /** Check if the completion of lesson led to the completion of the course **/
    private suspend fun checkCourseCompletion(updatedLessonCount: Int, courseId: String) {
        val totalLessons = courseRepository.getNumberOfLessonsForCourse(courseId)
        if (updatedLessonCount == totalLessons) {
            val studentId = student?.studentId ?: return
            studentCourseRepository.finishCourse(studentId, courseId)
            studentRepository.addCourseToFinished(studentId, courseId)
            studentRepository.removeCourseFromEnrolled(studentId,courseId, CourseType.VIDEO)
            courseStatisticsRepository.incrementCompletions(courseId,{},{})
            sharedPrefManager.updateFinishedCourse(courseId)
            _showCourseFinished.postValue(true)
        }
    }

    private suspend fun updateStudentLearningProgress(lessonId: String, courseId: String): Int {
        val dailyPlanId = getDailyPlanId()
        return if (!dailyLearningPlanRepository.isLessonCompleted(dailyPlanId, lessonId)) {
            incrementProgressAndStore(lessonId, courseId)
        } else {
            0
        }
    }

    private fun getDailyPlanId() = "${student?.studentId}_${dailyLearningPlanRepository.getCurrentDate()}"

    private suspend fun incrementProgressAndStore(lessonId: String, courseId: String): Int {
        val dailyPlan = _dailyLearningPlan.value ?: return 0

        _currentLessonIndex.postValue(dailyPlan.progress + 1)
        val newBrickCount = (_bricksCollected.value ?: 0) + 1
        _bricksCollected.value = newBrickCount

        val updatedLessonCount = updateStudentCourseProgress("${student?.studentId}_$courseId", lessonId)

        updateBricksCollected()
        updateDailyLearningPlanProgress(getDailyPlanId(), lessonId)

        return updatedLessonCount
    }

    private fun updateBricksCollected() {
        student?.let { st ->
            st.bricksCollected = _bricksCollected.value ?: 0
            sharedPrefManager.saveStudent(st)
            viewModelScope.launchSafe {
                studentRepository.incrementCollectedBricks(st.studentId)
            }
        }
    }

    private suspend fun updateStudentCourseProgress(studentCourseId: String, lessonId: String): Int {
        return studentCourseRepository.addLessonToCompleted(studentCourseId, lessonId)
    }

    private fun updateDailyLearningPlanProgress(dailyPlanId: String, lessonId: String) {
        viewModelScope.launchSafe {
            dailyLearningPlanRepository.completeLessonAndUpdateProgress(dailyPlanId, lessonId)
        }
    }

    private fun CoroutineScope.launchSafe(block: suspend () -> Unit) {
        launch {
            try {
                block()
            } catch (e: Exception) { }
        }
    }

    fun onCourseEnrollmentNavigationComplete() {
        _navigateToCourseEnrollment.value = false
    }
}
