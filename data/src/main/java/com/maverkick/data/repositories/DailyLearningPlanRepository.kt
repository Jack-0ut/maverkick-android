package com.maverkick.data.repositories

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.*
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
    suspend fun getDailyLearningPlanForStudent(studentId: String, dailyStudyTimeMinutes: Int): DailyLearningPlan {
        val existingPlan = getDailyLearningPlan(studentId)
        if (existingPlan != null && existingPlan.lessons.isNotEmpty()) {
            return existingPlan
        }

        // Try to fetch lessons and create a new plan
        val todayPairs = getTodayLessons(studentId, dailyStudyTimeMinutes * 60)
        if (todayPairs.isNotEmpty()) {
            val newPlan = createDailyLearningPlan(studentId, todayPairs)
            storeDailyLearningPlan(newPlan)
            return newPlan
        }

        throw Exception("Failed to generate a daily learning plan")
    }

    /** Create the daily learning plan object **/
    private fun createDailyLearningPlan(studentId: String, lessons: List<Lesson>): DailyLearningPlan {
        val totalDuration = lessons.sumOf{it.duration}
        return DailyLearningPlan(studentId, getCurrentDate(), lessons, totalDuration)
    }

    /** Store the daily plan in the database **/
    private fun storeDailyLearningPlan(plan: DailyLearningPlan) {
        databaseService.db.collection("dailyLearningPlans")
            .document("${plan.studentId}_${plan.date}").set(plan)
            .addOnSuccessListener {}
            .addOnFailureListener {}
    }

    /** Get the daily plan from the database **/
    private suspend fun getDailyLearningPlan(studentId: String): DailyLearningPlan? {
        val date = getCurrentDate()
        val docRef = databaseService.db.collection("dailyLearningPlans")
            .document("${studentId}_$date")
        val doc = suspendCoroutine { continuation ->
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        continuation.resumeWith(Result.success(document))
                    } else {
                        continuation.resumeWith(Result.success(null))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWith(Result.failure(exception))
                }
        }
        return convertToDailyLearningPlan(doc)
    }

    /*** A function that update daily learning plan on Course Enrollment **/
    suspend fun updateOnCourseEnrollment(studentId: String, courseId: String, courseType: CourseType, dailyLearningTimeMinutes: Int) {
        val todayPlan = getDailyLearningPlan(studentId) ?: return

        // If today's learning plan is completed, exit early
        if (todayPlan.status == DailyLearningPlanStatus.COMPLETED) return

        // Calculate the remaining time in seconds
        val remainedTimeSeconds = dailyLearningTimeMinutes * 60 - todayPlan.totalDuration

        // If there's no remaining time or only a minimal gap, exit early
        if (remainedTimeSeconds <= 120) return

        // Fetch lessons that fit within the remaining time for the specified course
        val lessonsToAdd = fetchLessonsThatFitsTime(courseId, studentId, courseType, remainedTimeSeconds)

        // If there are no lessons to add, exit early
        if (lessonsToAdd.isEmpty()) return

        // Add the lessons to the plan and update the duration
        val updatedPlan = todayPlan.copy(
            lessons = todayPlan.lessons + lessonsToAdd,
            totalDuration = todayPlan.totalDuration + lessonsToAdd.sumOf { it.duration }
        )

        storeDailyLearningPlan(updatedPlan)
    }

    /** A function that update daily learning plan on Course drop **/
    suspend fun updateOnCourseDrop(studentId: String, courseId: String, dailyLearningTimeSeconds: Int) {

        val todayPlan = getDailyLearningPlan(studentId) ?: return
        if (todayPlan.status == DailyLearningPlanStatus.COMPLETED || todayPlan.lessons.none { it.courseId == courseId }) { return }

        val lessonsToRemove = todayPlan.lessons.filter { it.courseId == courseId }
        val removedDuration = lessonsToRemove.sumOf { it.duration }
        val remainingTimeSeconds = dailyLearningTimeSeconds - (todayPlan.totalDuration - removedDuration)

        if (remainingTimeSeconds <= 120) { return }

        val additionalLessons = fillRemainingTimeWithLessons(studentId, remainingTimeSeconds)

        val updatedLessons = todayPlan.lessons - lessonsToRemove + additionalLessons

        val updatedDuration = todayPlan.totalDuration - removedDuration + additionalLessons.sumOf { it.duration }
        val removedLessonIds = lessonsToRemove.map { it.lessonId }.toSet()
        val updatedCompletedLessons = todayPlan.completedLessons.filterNot { it in removedLessonIds }

        val completedLessonIds = todayPlan.completedLessons
        val lessonsRemovedFromCompleted = removedLessonIds.intersect(completedLessonIds).size
        val updatedProgress = todayPlan.progress - lessonsRemovedFromCompleted

        val updatedPlan = todayPlan.copy(
            lessons = updatedLessons,
            totalDuration = updatedDuration,
            progress = updatedProgress,
            completedLessons = updatedCompletedLessons
        )
        storeDailyLearningPlan(updatedPlan)
    }


    /** A function that checks if a lesson is completed **/
    suspend fun isLessonCompleted(dailyLearningPlanId: String, lessonId: String): Boolean {
        val docRef =
            databaseService.db.collection("dailyLearningPlans").document(dailyLearningPlanId)

        val doc = docRef.get().await()

        val completedLessons = doc.get("completedLessons") as? List<*>
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
    private suspend fun getTodayLessons(studentId: String, dailyStudyTimeSeconds: Int): List<Lesson> {
        // Fetch the active courses for the student
        val courseDocs = fetchActiveCourses(studentId)

        val lessonQueues = mutableListOf<Queue<Lesson>>()

        // Fetch the lessons for each active course
        for (doc in courseDocs.documents) {
            val courseId = doc.getString("courseId") ?: continue
            val courseType = CourseType.valueOf(doc.getString("courseType") ?: continue)
            val lessons = fetchLessonsThatFitsTime(courseId, studentId, courseType, dailyStudyTimeSeconds)
            lessonQueues.add(LinkedList(lessons))
        }

        // Round-robin through the lesson queues
        val todayLessons = mutableListOf<Lesson>()
        var totalLessonTime = 0
        var i = 0

        while (totalLessonTime < dailyStudyTimeSeconds && lessonQueues.any { it.isNotEmpty() }) {
            val queue = lessonQueues[i % lessonQueues.size]
            if (queue.isNotEmpty()) {
                val lesson = queue.peek()
                val lessonTime = lesson!!.duration
                // Add the lesson to the list if there is enough remaining study time
                if (totalLessonTime + lessonTime > dailyStudyTimeSeconds) {
                    break // If the next lesson doesn't fit, stop adding lessons
                }
                todayLessons.add(lesson)
                totalLessonTime += lessonTime
                queue.poll() // now that we're sure we're using this lesson, remove it from the queue
            }
            i++
        }
        return todayLessons
    }

    /** For a given course and student, find the number of the lesson student finished in that course **/
    private suspend fun getLastWatchedLessonOrder(courseId: String, studentId: String): Int {
        // Fetch the progress record for the student-course pair
        val progressDoc = suspendCoroutine<DocumentSnapshot> { continuation ->
            databaseService.db.collection("studentCourseProgress")
                .document("${studentId}_$courseId").get()
                .addOnSuccessListener { documentSnapshot -> continuation.resume(documentSnapshot) }
                .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
        }
        return progressDoc.getLong("lastCompletedLesson")?.toInt() ?: 0
    }

    /** This function fetches lessons for each active course and fills the remaining time with those lessons in a round-robin manner **/
    private suspend fun fillRemainingTimeWithLessons(studentId: String, remainingTimeSeconds: Int): List<Lesson> {
        val lessonQueues = mutableListOf<Queue<Lesson>>()

        // Fetch the active courses for the student
        val courseDocs = fetchActiveCourses(studentId)

        // Fetch the lessons for each active course, considering the remaining time
        for (doc in courseDocs.documents) {
            val courseId = doc.getString("courseId") ?: continue
            val courseType = CourseType.valueOf(doc.getString("courseType") ?: continue)
            val lessons = fetchLessonsThatFitsTime(courseId, studentId, courseType, remainingTimeSeconds)
            lessonQueues.add(LinkedList(lessons))
        }

        val filledLessons = mutableListOf<Lesson>()
        var totalLessonTime = 0
        var i = 0

        // Round-robin through the lesson queues
        while (totalLessonTime < remainingTimeSeconds && lessonQueues.any { it.isNotEmpty() }) {
            val queue = lessonQueues[i % lessonQueues.size]
            if (queue.isNotEmpty()) {
                val lesson = queue.peek()
                val lessonTime = lesson!!.duration

                // If the next lesson doesn't fit, stop adding lessons
                if (totalLessonTime + lessonTime > remainingTimeSeconds) {
                    break
                }

                filledLessons.add(lesson)
                totalLessonTime += lessonTime
                queue.poll()
            }
            i++
        }

        return filledLessons
    }

    private suspend fun fetchLessonsThatFitsTime(courseId: String, studentId: String, courseType: CourseType, remainingStudyTimeSeconds: Int): List<Lesson> {
        return when (courseType) {
            CourseType.TEXT_PERSONALIZED -> getNewTextCourseLessons(courseId, studentId, remainingStudyTimeSeconds, "generatedCourses")
            CourseType.TEXT -> getNewTextCourseLessons(courseId, studentId, remainingStudyTimeSeconds, "courses")
            CourseType.VIDEO -> getNewVideoCourseLessons(courseId, studentId, remainingStudyTimeSeconds, "courses")
        }
    }

    private suspend fun getNewTextCourseLessons(courseId: String, studentId: String, remainingStudyTimeSeconds: Int, collectionName: String): List<TextLesson> {
        // Adjust your getNewCourseLessonsTemplate function to accept collectionName as a parameter and use it in the query
        return getNewCourseLessonsTemplate(courseId, studentId, remainingStudyTimeSeconds, collectionName, TextLessonFirebase::class.java)
    }

    private suspend fun getNewVideoCourseLessons(courseId: String, studentId: String, remainingStudyTimeSeconds: Int, collectionName: String): List<VideoLesson> {
        // Adjust your getNewCourseLessonsTemplate function to accept collectionName as a parameter and use it in the query
        return getNewCourseLessonsTemplate(courseId, studentId, remainingStudyTimeSeconds, collectionName, VideoLessonFirebase::class.java)
    }

    private suspend fun <T : Lesson, F : LessonFirebase> getNewCourseLessonsTemplate(
        courseId: String, studentId: String, desiredTimeSeconds: Int, collectionName: String, lessonFirebaseClass: Class<F>): List<T> {

        if (desiredTimeSeconds <= 120) {
            return emptyList()
        }

        val lastWatchedLessonOrder = getLastWatchedLessonOrder(courseId, studentId)
        val newLessons = mutableListOf<T>()
        var totalLessonTime = 0
        var lessonOffset = lastWatchedLessonOrder + 1

        while (totalLessonTime < desiredTimeSeconds) {
            val lessonRef = databaseService.db.collection(collectionName).document(courseId)
                .collection("lessons")
                .orderBy("lessonOrder")
                .startAt(lessonOffset)
                .limit(1)

            val lesson = lessonRef.get().await().documents.mapNotNull { doc ->
                doc.toObject(lessonFirebaseClass)?.toLesson(courseId, doc.id) as? T
            }.firstOrNull()

            if (lesson != null && totalLessonTime + lesson.duration <= desiredTimeSeconds) {
                newLessons.add(lesson)
                totalLessonTime += lesson.duration
                lessonOffset++
            } else {
                break
            }
        }
        return newLessons
    }

    /** Function that fetches active courses for a student from a database **/
    private suspend fun fetchActiveCourses(studentId: String): QuerySnapshot {
        return try {
            databaseService.db.collection("studentCourses")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("active", true)
                .get()
                .await()
        } catch (exception: Exception) {
            throw exception
        }
    }

    /** Convert document to the DailyLearningPlan Object **/
    private fun convertToDailyLearningPlan(doc: DocumentSnapshot?): DailyLearningPlan? {
        val studentId = doc?.getString("studentId") ?: return null
        val date = doc.getString("date")
        val totalDuration = doc.getLong("totalDuration")?.toInt() ?: 0
        val progress = doc.getLong("progress")?.toInt() ?: 0
        val statusString = doc.getString("status") ?: "PLANNED"
        val status = DailyLearningPlanStatus.valueOf(statusString)

        // Get the completed lessons as a generic list and cast it if necessary
        val completedLessons = doc.get("completedLessons") as? List<String> ?: emptyList()

        val lessonMaps = doc.get("lessons") as? List<Map<String, Any>> ?: emptyList()
        val lessons = lessonMaps.mapNotNull { convertToLesson(it) }

        return DailyLearningPlan(studentId, date, lessons, totalDuration, progress, status, completedLessons)
    }

    /** Convert lesson map to the particular Lesson object **/
    private fun convertToLesson(lessonMap: Map<String, Any>): Lesson? {
        val courseId = lessonMap["courseId"] as? String ?: ""
        val lessonId = lessonMap["lessonId"] as? String ?: ""
        val title = lessonMap["title"] as? String ?: ""
        val duration = (lessonMap["duration"] as? Long ?: 0L).toInt()
        val lessonOrder = (lessonMap["lessonOrder"] as? Long ?: 0L).toInt()

        return if ("content" in lessonMap) {
            val content = lessonMap["content"] as? String ?: ""
            TextLesson(lessonId, courseId, title, duration, lessonOrder, content)
        } else if ("transcription" in lessonMap) {
            val videoUrl = lessonMap["videoUrl"] as? String ?: ""
            val transcription = lessonMap["transcription"] as? String ?: ""
            val creationDate = lessonMap["creationDate"] as? Date ?: Date()
            VideoLesson(lessonId, courseId, title, duration, lessonOrder, videoUrl, transcription, creationDate)
        } else {
            null
        }
    }

    /** Get the current date in format YYYY-MM-DD as a string **/
    fun getCurrentDate(): String {
        return LocalDate.now().toString()
    }
}
