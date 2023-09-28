package com.maverkick.data.models

/**
 * Class that stores the list of lessons the student should learn the particular day
 **/
data class DailyLearningPlan(
    val studentId: String,
    val date: String?,
    val lessons: List<Lesson>, // Interface or abstract class for different lesson types
    val totalDuration: Int,
    var progress: Int = 0,
    var status: DailyLearningPlanStatus = DailyLearningPlanStatus.PLANNED, // Enum or class for status
    var completedLessons: List<String> = emptyList() // List of completed lesson IDs or objects
) {
    fun incrementProgress(lessonId: String) {
        if (progress < lessons.size) {
            progress++
            completedLessons = completedLessons + lessonId
        }
    }
}

enum class DailyLearningPlanStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED
}
