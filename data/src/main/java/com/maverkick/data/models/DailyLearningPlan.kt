package com.maverkick.data.models

/**
 * Class that stores the list of lessons the student should learn the particular day
 **/
data class DailyLearningPlan(
    val studentId: String,
    val date: String, // Date represented in a format like "yyyy-mm-dd"
    val lessons: List<Lesson>,
    val totalDuration: Int,
    var progress: Int = 0 // Number of lessons completed
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", emptyList(), 0, 0)

    fun incrementProgress() {
        if (progress <= lessons.size) {
            progress++
        }
    }
}
