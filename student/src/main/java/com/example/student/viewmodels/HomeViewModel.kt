package com.example.student.viewmodels

import androidx.lifecycle.*
import com.example.data.models.DailyLearningPlan
import com.example.data.repositories.DailyLearningPlanRepository
import com.example.data.repositories.StudentCourseRepository
import com.example.data.repositories.StudentRepository
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel for the HomeFragment.
 * This ViewModel interacts with the data sources,updates the progress of the student and exposes LiveData objects
 * that the Fragment can observe to update the UI.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dailyLearningPlanRepository: DailyLearningPlanRepository,
    private val studentCourseRepository: StudentCourseRepository,
    private val studentRepository: StudentRepository,
    private val sharedPrefManager: SharedPrefManager
): ViewModel() {

    private val _dailyLearningPlan = MutableLiveData<DailyLearningPlan>()
    val dailyLearningPlan: LiveData<DailyLearningPlan> get() = _dailyLearningPlan

    private val _currentLessonIndex = MutableLiveData<Int>()
    val currentLessonIndex: LiveData<Int> get() = _currentLessonIndex

    private val _navigateToCourseEnrollment = MutableLiveData<Boolean>()
    val navigateToCourseEnrollment: LiveData<Boolean> get() = _navigateToCourseEnrollment

    // check if we finished all the lessons
    val isLastLesson: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var lastIndex: Int? = null
        var lessonsSize: Int? = null

        addSource(_currentLessonIndex) { index ->
            lastIndex = index
            val size = lessonsSize
            if (index != null && size != null) {
                value = index >= size - 1
            }
        }

        addSource(_dailyLearningPlan) { plan ->
            lessonsSize = plan.lessons.size
            val index = lastIndex
            if (index != null && lessonsSize != null) {
                value = index >= lessonsSize!! - 1
            }
        }
    }

    private val _bricksCollected = MutableLiveData<Int>()
    val bricksCollected: LiveData<Int> get() = _bricksCollected

    private val studentId: String = sharedPrefManager.getStudent()?.studentId ?: ""
    private val dailyStudyTimeMinutes = sharedPrefManager.getStudent()?.dailyStudyTimeMinutes ?: 0

    init {
        checkIfStudentEnrolledInAnyCourse()
        loadBricksCollected()
    }
    /** Check if student enrolled in any course, if not initiate navigation to the gallery **/
    private fun checkIfStudentEnrolledInAnyCourse() {
        val student = sharedPrefManager.getStudent()
        student?.let {
            if (it.enrolledCourses.isEmpty()) {
                _navigateToCourseEnrollment.postValue(true)
            } else {
                // load daily learning plan only if student is enrolled in a course
                loadDailyLearningPlan()
            }
        }
    }

    /** Load the daily learning plan for today **/
    private fun loadDailyLearningPlan() {
        viewModelScope.launch {
            val plan = dailyLearningPlanRepository.fetchOrGenerateDailyPlan(studentId, dailyStudyTimeMinutes)
            _dailyLearningPlan.value = plan
            _currentLessonIndex.value = plan.progress
        }
    }

    /** Load the initial number of bricks collected **/
    private fun loadBricksCollected() {
        _bricksCollected.value = sharedPrefManager.getStudent()?.bricksCollected
    }

    /** Student has at least 1 course, so we won't do the navigation to gallery then **/
    fun onCourseEnrollmentNavigationComplete() {
        _navigateToCourseEnrollment.value = false
    }

    /** Update learning progress for DailyLearningPlan and StudentCourseProgress **/
    suspend fun updateStudentLearningProgress(lessonId: String, courseId: String) {
        val dailyPlanId = "${studentId}_${_dailyLearningPlan.value?.date}"
        if (!dailyLearningPlanRepository.isLessonCompleted(dailyPlanId,lessonId)) {
            incrementProgressAndStore(lessonId, courseId)
        }
    }

    /** Update the progress **/
    private fun incrementProgressAndStore(lessonId: String, courseId: String) {
        val dailyPlan = _dailyLearningPlan.value
        if (dailyPlan != null) {
            dailyPlan.incrementProgress()
            _dailyLearningPlan.value = dailyPlan!!
            _currentLessonIndex.value = dailyPlan.progress

            // Increment the number of bricks collected
            _bricksCollected.value = (_bricksCollected.value ?: 0) + 1

            val dailyPlanId = "${studentId}_${dailyPlan.date}"
            updateDailyLearningPlanProgress(dailyPlanId,lessonId)

            val studentCoursesId = "${studentId}_$courseId"
            updateStudentCourseProgress(studentCoursesId, lessonId)

            // Update the number of bricks collected in the SharedPreferences
            updateBricksCollected()

            // Update the number of bricks collected in the database
            updateStudentLessonsFinishedInDatabase()
        }else{
            loadDailyLearningPlan()
        }
    }

    /** Update the StudentCoursesProgress by adding new lesson to the completed list **/
    private fun updateStudentCourseProgress(studentCourseId:String, lessonId:String){
        viewModelScope.launch {
            studentCourseRepository.addLessonToCompleted(studentCourseId, lessonId)
        }
    }

    /** Update progress to the daily learning plan in the database **/
    private fun updateDailyLearningPlanProgress(dailyPlanId:String, lessonId: String) {
        viewModelScope.launch {
            dailyLearningPlanRepository.completeLessonAndUpdateProgress(dailyPlanId, lessonId)
        }
    }

    /** Update the number of bricks collected in the SharedPreferences **/
    private fun updateBricksCollected() {
        sharedPrefManager.getStudent()?.let {
            it.bricksCollected = _bricksCollected.value ?: 0
            sharedPrefManager.saveStudent(it)
        }
    }

    /** Update the number of bricks collected in the database **/
    private fun updateStudentLessonsFinishedInDatabase() {
        viewModelScope.launch {
            studentRepository.incrementCollectedBricks(studentId)
        }
    }
}
