package com.example.data.repositories

import com.example.data.IDatabaseService
import com.example.data.models.CourseStatistics
import com.example.data.models.FirebaseCourseStatistics
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CourseStatisticsRepository @Inject constructor(
    private val databaseService: IDatabaseService
) {

    /** Add new CourseStatistics for the given course id **/
    fun addCourseStatistics(courseId: String, courseName: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val courseStatistics = CourseStatistics(courseId, courseName, 0, 0, 0, 0)
        val firebaseCourseStatistics = courseStatistics.toFirebaseCourseStatistics()
        databaseService.db.collection("courseStatistics")
            .document(courseId)
            .set(firebaseCourseStatistics)
            .addOnSuccessListener {
                onSuccess(courseId)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


    /** Increment enrollments for the given course id **/
    fun incrementEnrollments(courseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courseStatistics")
            .document(courseId)
            .update("numberOfEnrollments", FieldValue.increment(1))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /** Increment unEnrollments for the given course id **/
    fun incrementDropouts(courseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courseStatistics")
            .document(courseId)
            .update("numberOfDropouts", FieldValue.increment(1))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /** Fetch CourseStatistics for the given course id **/
    suspend fun getCourseStatistics(courseId: String): CourseStatistics? = withContext(Dispatchers.IO) {
        return@withContext try {
            val documentSnapshot = databaseService.db.collection("courseStatistics")
                .document(courseId)
                .get()
                .await() // await() is used to wait for the result in coroutines

            val firebaseCourseStatistics = documentSnapshot.toObject(FirebaseCourseStatistics::class.java)
            val courseStatistics = firebaseCourseStatistics?.toCourseStatistics(courseId)

            courseStatistics
        } catch (e: Exception) {
            null
        }
    }

    /** Update ratings for the given course id **/
    fun updateRatings(courseId: String, rating: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val docRef = databaseService.db.collection("courseStatistics").document(courseId)

        databaseService.db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentTotalRatings = snapshot.getDouble("totalNumberOfRatings") ?: 0.0
            val currentSumOfRatings = snapshot.getDouble("sumOfRatings") ?: 0.0

            transaction.update(docRef, "totalNumberOfRatings", currentTotalRatings + 1)
            transaction.update(docRef, "sumOfRatings", currentSumOfRatings + rating)
        }
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}
