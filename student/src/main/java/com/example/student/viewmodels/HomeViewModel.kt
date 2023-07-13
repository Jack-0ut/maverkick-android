package com.example.student.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.DailyLearningPlan
import com.example.data.repositories.DailyLearningPlanRepository
import com.example.data.repositories.StudentCourseRepository
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
    sharedPrefManager: SharedPrefManager
): ViewModel() {

    private val _dailyLearningPlan = MutableLiveData<DailyLearningPlan>()
    val dailyLearningPlan: LiveData<DailyLearningPlan> get() = _dailyLearningPlan

    private val _currentLessonIndex = MutableLiveData<Int>()
    val currentLessonIndex: LiveData<Int> get() = _currentLessonIndex

    private val studentId: String = sharedPrefManager.getStudent()?.studentId ?: ""
    private val dailyStudyTimeMinutes = sharedPrefManager.getStudent()?.dailyStudyTimeMinutes ?: 0

    init {
        loadDailyLearningPlan()
    }

    /** Update learning progress for DailyLearningPlan and StudentCourseProgress **/
    suspend fun updateStudentLearningProgress(lessonId: String, courseId: String) {
        val dailyPlanId = "${studentId}_${_dailyLearningPlan.value?.date}"
        if (!dailyLearningPlanRepository.isLessonCompleted(dailyPlanId,lessonId)) {
            incrementProgressAndStore(lessonId, courseId)
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

    /** Update the progress **/
    private fun incrementProgressAndStore(lessonId: String, courseId: String) {
        val dailyPlan = _dailyLearningPlan.value
        dailyPlan?.incrementProgress()
        _dailyLearningPlan.value = dailyPlan!!
        _currentLessonIndex.value = dailyPlan.progress

        val dailyPlanId = "${studentId}_${dailyPlan.date}"
        updateDailyLearningPlanProgress(dailyPlanId)

        val studentCoursesId = "${studentId}_$courseId"
        updateStudentCourseProgress(studentCoursesId, lessonId)
    }

    /** Update the StudentCoursesProgress by adding new lesson to the completed list **/
    private fun updateStudentCourseProgress(studentCourseId:String, lessonId:String){
        viewModelScope.launch {
            studentCourseRepository.addLessonToCompleted(studentCourseId, lessonId)
        }
    }

    /** Update progress to the daily learning plan in the database **/
    private fun updateDailyLearningPlanProgress(dailyPlanId:String) {
        dailyLearningPlanRepository.incrementProgress(dailyPlanId)
    }
}
