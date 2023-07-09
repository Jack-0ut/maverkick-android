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
    private val sharedPrefManager: SharedPrefManager
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

    fun updateStudentLearningProgress(lessonId: String, courseId: String) {
        if (!isLessonInCompleted(lessonId)) {
            incrementProgressAndStore(lessonId, courseId)
            addLessonToCompletedInPreferences(lessonId)
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

    private fun incrementProgressAndStore(lessonId: String, courseId: String) {
        val dailyPlan = _dailyLearningPlan.value
        dailyPlan?.incrementProgress()
        _dailyLearningPlan.value = dailyPlan!!
        _currentLessonIndex.value = dailyPlan.progress

        incrementProgressInFirestore(dailyPlan.studentId, dailyPlan.date)

        val studentCoursesId = "${studentId}_$courseId"
        updateStudentCourseProgress(studentCoursesId, lessonId)
    }

    /** Update the StudentCoursesProgress by adding new lesson to the completed list **/
    private fun updateStudentCourseProgress(studentCourseId:String, lessonId:String){
        viewModelScope.launch {
            studentCourseRepository.addLessonToCompleted(studentCourseId, lessonId)
        }
    }

    /** Store progress to the daily learning plan in the database **/
    private fun incrementProgressInFirestore(studentId: String, date: String) {
        viewModelScope.launch {
            val dailyLearningPlanId = "$studentId-$date"
            dailyLearningPlanRepository.incrementProgress(dailyLearningPlanId)
        }
    }

    /**Add lesson to a list of completed lessons locally **/
    private fun addLessonToCompletedInPreferences(lessonId: String) {
        sharedPrefManager.addLessonToCompleted(lessonId)
    }

    /** Check local list of completed lessons and tells if given lesson is in there **/
    private fun isLessonInCompleted(lessonId: String): Boolean {
        return sharedPrefManager.getCompletedLessonsMap().containsKey(lessonId)
    }
}
