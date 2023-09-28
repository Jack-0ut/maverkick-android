package com.maverkick.student.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.maverkick.data.models.DailyLearningPlan
import com.maverkick.data.repositories.DailyLearningPlanRepository
import com.maverkick.data.repositories.StudentCourseRepository
import com.maverkick.data.repositories.StudentRepository
import com.maverkick.data.sharedpref.SharedPrefManager
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

    private val _courseGenerationTries = MutableLiveData<Int>()
    val courseGenerationTries: LiveData<Int> get() = _courseGenerationTries

    private val studentId: String = sharedPrefManager.getStudent()?.studentId ?: ""
    private val dailyStudyTimeMinutes = sharedPrefManager.getStudent()?.dailyStudyTimeMinutes ?: 0

    init {
        checkIfStudentEnrolledInAnyCourse()
        loadBricksCollected()
        loadCourseGenerationTries()
    }

    /** Check if student enrolled in any course, if not initiate navigation to the gallery **/
    fun checkIfStudentEnrolledInAnyCourse() {
        val student = sharedPrefManager.getStudent()
        student?.let {
            if (it.enrolledVideoCourses.isEmpty() && it.enrolledTextCourses.isEmpty()) {
                _navigateToCourseEnrollment.postValue(true)
            } else {
                loadDailyLearningPlan()
            }
        }
    }

    /** Load the daily learning plan for today **/
    private fun loadDailyLearningPlan() {
        viewModelScope.launch {
            try {
                val plan = dailyLearningPlanRepository.getDailyLearningPlanForStudent(studentId, dailyStudyTimeMinutes)
                _dailyLearningPlan.value = plan
                _currentLessonIndex.value = plan.progress
            } catch (exception: Exception) {
                Log.e("DailyLearningPlan", "Error loading daily learning plan for studentId: $studentId", exception)
            }
        }
    }

    /** Load the initial number of bricks collected **/
    private fun loadBricksCollected() {
        _bricksCollected.value = sharedPrefManager.getStudent()?.bricksCollected
    }

    /** Load the course generation tries **/
    private fun loadCourseGenerationTries() {
        _courseGenerationTries.value = sharedPrefManager.getStudent()?.courseGenerationTries
    }

    /** Update the course generation tries in the shared preferences **/
    fun updateCourseGenerationTries() {
        sharedPrefManager.getStudent()?.let { student ->
            val currentTries = student.courseGenerationTries
            student.courseGenerationTries = currentTries - 1
            sharedPrefManager.saveStudent(student)
        }
    }

    /** Student has at least 1 course, so we won't do the navigation to gallery then **/
    fun onCourseEnrollmentNavigationComplete() {
        _navigateToCourseEnrollment.value = false
    }

    /** Updates the student's learning progress for a specific lesson and course. */
    private suspend fun updateStudentLearningProgress(lessonId: String, courseId: String) {
        val dailyPlanId = getDailyPlanId()
        if (!dailyLearningPlanRepository.isLessonCompleted(dailyPlanId, lessonId)) {
            incrementProgressAndStore(lessonId, courseId)
        }
    }

    /** Generates the daily plan ID based on the student ID and the current date.*/
    private fun getDailyPlanId() = "${studentId}_${dailyLearningPlanRepository.getCurrentDate()}"

    /**
     * Increments the progress and stores the updated values for the specified lesson and course.
     * This includes incrementing the lesson progress and the number of bricks collected.
     */
    private fun incrementProgressAndStore(lessonId: String, courseId: String) {
        val dailyPlan = _dailyLearningPlan.value ?: return

        _dailyLearningPlan.value = dailyPlan
        _currentLessonIndex.postValue(dailyPlan.progress + 1)

        val newBrickCount = (_bricksCollected.value ?: 0) + 1
        _bricksCollected.value = newBrickCount
        val dailyPlanId = getDailyPlanId()

        updateDailyLearningPlanProgress(dailyPlanId, lessonId)
        updateStudentCourseProgress("${studentId}_$courseId", lessonId)
        updateBricksCollected()
    }

    /**
     * Updates the number of bricks collected both in SharedPreferences and the database.
     * This function takes care of saving the updated brick count to the local preferences
     * and also increments the collected bricks count in the database.
     */
    private fun updateBricksCollected() {
        sharedPrefManager.getStudent()?.let { student ->
            val newBrickCount = _bricksCollected.value ?: 0
            student.bricksCollected = newBrickCount
            sharedPrefManager.saveStudent(student)
        }
        viewModelScope.launch {
            studentRepository.incrementCollectedBricks(studentId)
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

    /**
     * Handles the completion of a lesson by a student within a given course.
     * Usage:
     * - Should be invoked whenever a lesson is marked as completed by a student.
     * - Typically used within a ViewModel scope to encapsulate the business logic related to lesson completion.
     */
    fun handleLessonCompleted(lessonId: String, courseId: String) {
        viewModelScope.launch {
            updateStudentLearningProgress(lessonId, courseId)
        }
    }
}
