package com.maverkick.data.models

/**
 * Data class to hold statistics for a specific course.
 *
 * @property courseId Unique identifier of the course.
 * @property numberOfEnrollments Total number of students that have enrolled in the course.
 * @property numberOfCompletions Total number of students that have completed the course.
 * @property numberOfDropouts Total number of students that have dropped out of the course.
 * @property totalNumberOfRatings Total number of ratings given to the course.
 * @property sumOfRatings Sum of all ratings given to the course.
 */
data class CourseStatistics(
    val courseId: String,
    var courseName: String,
    var numberOfEnrollments: Int = 0,
    var numberOfCompletions: Int = 0,
    var numberOfDropouts: Int = 0,
    var totalNumberOfRatings: Int = 0,
    var sumOfRatings: Int = 0
) {
    // No-argument constructor required for Firestore
    constructor() : this("","", 0, 0, 0, 0, 0)

    /**
     * Calculates the completion rate of the course.
     * The completion rate is defined as the number of completions divided by the number of enrollments.
     *
     * @return The completion rate, or 0 if there are no enrollments.
     */
    fun calculateCompletionRate(): Double = if (numberOfEnrollments > 0) numberOfCompletions.toDouble() / numberOfEnrollments else 0.0

    /**
     * Calculates the dropout rate of the course.
     * The dropout rate is defined as the number of dropouts divided by the number of enrollments.
     *
     * @return The dropout rate, or 0 if there are no enrollments.
     */
    fun calculateDropoutRate(): Double = if (numberOfEnrollments > 0) numberOfDropouts.toDouble() / numberOfEnrollments else 0.0

    /**
     * Calculates the average rating of the course.
     * The average rating is defined as the sum of all ratings divided by the total number of ratings.
     *
     * @return The average rating, or 0 if there are no ratings.
     */
    fun calculateAverageRating(): Double = if (totalNumberOfRatings > 0) sumOfRatings.toDouble() / totalNumberOfRatings else 0.0

    fun toFirebaseCourseStatistics(): FirebaseCourseStatistics {
        return FirebaseCourseStatistics(
            courseName = this.courseName,
            numberOfEnrollments = this.numberOfEnrollments,
            numberOfCompletions = this.numberOfCompletions,
            numberOfDropouts = this.numberOfDropouts,
            totalNumberOfRatings = this.totalNumberOfRatings,
            sumOfRatings = this.sumOfRatings
        )
    }
}


data class FirebaseCourseStatistics @JvmOverloads constructor(
    var courseName: String = "",
    var numberOfEnrollments: Int = 0,
    var numberOfCompletions: Int = 0,
    var numberOfDropouts: Int = 0,
    var totalNumberOfRatings: Int = 0,
    var sumOfRatings: Int = 0
) {
    fun toCourseStatistics(courseId: String): CourseStatistics {
        return CourseStatistics(
            courseId,
            courseName,
            numberOfEnrollments,
            numberOfCompletions,
            numberOfDropouts,
            totalNumberOfRatings,
            sumOfRatings
        )
    }
}

enum class StatisticType {
    ENROLLMENTS,
    COMPLETION_RATE,
    DROPOUTS,
    AVERAGE_RATING
}

