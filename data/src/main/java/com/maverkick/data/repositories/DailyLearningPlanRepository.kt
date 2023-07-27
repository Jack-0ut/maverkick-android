package com.maverkick.data.repositories

import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.DailyLearningPlan
import com.maverkick.data.models.Lesson
import com.maverkick.data.models.LessonFirebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Class responsible for creation and management of the Daily Learning Plan
 * for the particular student
 * @param databaseService - database to which we're connecting
 **/

class DailyLearningPlanRepository @Inject constructor(private val databaseService: IDatabaseService){

    /** Check if learning plan exists **/
    suspend fun fetchOrGenerateDailyPlan(studentId: String, dailyStudyTimeMinutes: Int): DailyLearningPlan {
        val today = LocalDate.now().toString()
        var existingPlan = getDailyLearningPlan(studentId, today)

        if (existingPlan != null && existingPlan.lessons.isNotEmpty()) {
            return existingPlan
        } else {
            var todayPairs = getTodayLessons(studentId, dailyStudyTimeMinutes)
            if (todayPairs.isNotEmpty()) {
                val newPlan = createDailyLearningPlan(studentId, todayPairs)
                storeDailyLearningPlan(newPlan)
                existingPlan = newPlan
            } else {
                // No lessons today, try to fetch or generate the daily plan again
                todayPairs = getTodayLessons(studentId, dailyStudyTimeMinutes)
                if (todayPairs.isNotEmpty()) {
                    val newPlan = createDailyLearningPlan(studentId, todayPairs)
                    storeDailyLearningPlan(newPlan)
                    existingPlan = newPlan
                }
            }
        }
        return existingPlan ?: throw Exception("Failed to generate a daily learning plan for $studentId")
    }


    /** Create the daily learning plan object **/
    private fun createDailyLearningPlan(studentId: String, lessons: List<Lesson>): DailyLearningPlan {
        val totalDuration = lessons.sumOf{it.duration}
        return DailyLearningPlan(studentId, LocalDate.now().toString(), lessons, totalDuration)
    }

    /** Store the daily plan in the database **/
    private fun storeDailyLearningPlan(plan: DailyLearningPlan) {
        databaseService.db.collection("dailyLearningPlans")
            .document("${plan.studentId}_${plan.date}")
            .set(plan)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    /** Get the daily plan from the database **/
    private suspend fun getDailyLearningPlan(studentId: String, date: String): DailyLearningPlan? {
        val docRef = databaseService.db.collection("dailyLearningPlans")
            .document("${studentId}_$date")
        val doc = suspendCoroutine { continuation ->
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        continuation.resumeWith(Result.success(document))
                    } else {
                        continuation.resumeWith(Result.success(null))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWith(Result.failure(exception))
                }
        }
        return doc?.toObject(DailyLearningPlan::class.java)
    }

    /** A function that checks if a lesson is completed **/
    suspend fun isLessonCompleted(dailyLearningPlanId: String, lessonId: String): Boolean {
        // Get the document reference
        val docRef = databaseService.db.collection("dailyLearningPlans").document(dailyLearningPlanId)

        // Get the document
        val doc = docRef.get().await()

        // Get the completed lessons list
        val completedLessons = doc.get("completedLessons") as? List<String>

        // Check if the lesson is completed
        return completedLessons?.contains(lessonId) ?: false
    }

    /** A function that adds a lesson to the list of completed lessons and increment progress by 1**/
    suspend fun completeLessonAndUpdateProgress(dailyPlanId: String, lessonId: String) {
        val docRef = databaseService.db.collection("dailyLearningPlans").document(dailyPlanId)

        databaseService.db.runBatch { batch ->
            // Add the lesson to the completed lessons
            batch.update(docRef, "completedLessons", FieldValue.arrayUnion(lessonId))
            // Increment the progress
            batch.update(docRef, "progress", FieldValue.increment(1))
        }.await()
    }


    /** Get the list of today's lessons for the student **/
    private suspend fun getTodayLessons(studentId: String, dailyStudyTimeMinutes: Int): List<Lesson> {
        val dailyStudyTime = dailyStudyTimeMinutes * 60
        // Fetch the active courses for the student
        val courseDocs = try {
            databaseService.db.collection("studentCourses")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("active", true)
                .get()
                .await()
        } catch (exception: Exception) {
            throw exception
        }

        val courseIds = courseDocs.documents.mapNotNull { it.getString("courseId") }
        val lessonQueues = mutableListOf<Queue<Lesson>>()

        // Fetch the lessons for each active course
        for (courseID in courseIds) {
            // Here, you would get the lastWatchedLessonOrder for the course, then add 1 to it to get the next lesson
            val lastWatchedLessonOrder = getLastWatchedLessonOrder(courseID, studentId)
            val lessonDocs = suspendCoroutine<QuerySnapshot> { continuation ->
                databaseService.db.collection("courses").document(courseID)
                    .collection("lessons")
                    .orderBy("lessonOrder")
                    .startAt(lastWatchedLessonOrder + 1)
                    .limit(6)  // fetch up to 6 lessons at a time
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        continuation.resume(querySnapshot)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            val lessons = lessonDocs.documents.mapNotNull {
                it.toObject(LessonFirebase::class.java)?.toLesson(courseID,it.id)
            }
            lessonQueues.add(LinkedList(lessons))
        }

        // Round-robin through the lesson queues
        val todayLessons = mutableListOf<Lesson>()
        var totalLessonTime = 0
        var i = 0

        while (totalLessonTime < dailyStudyTime && lessonQueues.any { it.isNotEmpty() }) {
            val queue = lessonQueues[i % lessonQueues.size]
            if (queue.isNotEmpty()) {
                val lesson = queue.peek()  // just peek at the next lesson, don't remove it yet
                val lessonTime = lesson!!.duration
                // Add the lesson to the list if there is enough remaining study time
                if (totalLessonTime + lessonTime > dailyStudyTime) {
                    break // If the next lesson doesn't fit, stop adding lessons
                }
                todayLessons.add(lesson)  // add the lesson to the list
                totalLessonTime += lessonTime
                queue.poll()  // now that we're sure we're using this lesson, remove it from the queue
            }
            i++
        }
        todayLessons.sortBy { it.courseId }
        return todayLessons
    }

    /** For a given course and student, find the number of the lesson student finished in that course **/
    private suspend fun getLastWatchedLessonOrder(courseId: String, studentId: String): Int {
        // Fetch the progress record for the student-course pair
        val progressDoc = suspendCoroutine<DocumentSnapshot> { continuation ->
            databaseService.db.collection("studentCourseProgress")
                .document("${studentId}_$courseId")
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    continuation.resume(documentSnapshot)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
        return progressDoc.getLong("lastCompletedLesson")?.toInt() ?: 0
    }
}
