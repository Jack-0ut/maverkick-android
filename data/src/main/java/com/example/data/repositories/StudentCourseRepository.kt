package com.example.data.repositories

import android.util.Log
import com.example.data.IDatabaseService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Describes interaction between student and it's courses
 **/
class StudentCourseRepository @Inject constructor(private val databaseService: IDatabaseService){

    /** Add the lesson to the list of completed for a given student's course**/
    fun addLessonToCompleted(studentCourseId: String, lessonId: String) {
        Log.d("StudentCourseRepo", "Updating student course with completed lesson. Student Course ID: $studentCourseId, Lesson ID: $lessonId")

        val studentCourseRef = databaseService.db.collection("studentCourseProgress").document(studentCourseId)

        databaseService.db.runTransaction { transaction ->
            transaction.update(studentCourseRef, "completedLessons", FieldValue.arrayUnion(lessonId))
            transaction.update(studentCourseRef, "lastCompletedLesson", FieldValue.increment(1))
        }
    }

    /** Enroll student in the particular course **/
    suspend fun enrollStudent(studentId: String, courseId: String) {
        // Perform the enrollment operation in the Firebase Firestore or any other remote data source
        val studentCoursesCollection = databaseService.db.collection("studentCourses")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseDocument = studentCoursesCollection.document(studentCourseDocumentId).get().await()

        if (!studentCourseDocument.exists()) {
            // If the document does not exist, enroll the student in the course
            val newStudentCourse = hashMapOf(
                "studentId" to studentId,
                "courseId" to courseId,
                "enrollmentDate" to Timestamp.now(), // Use the current time as the enrollment date
                "active" to true
            )

            studentCoursesCollection.document(studentCourseDocumentId).set(newStudentCourse).await()
        } else {
            throw Exception("Student already enrolled in the course")
        }
    }

    /** Init student course progress collection **/
    suspend fun initStudentCourseProgress(studentId: String, courseId: String) {
        // Perform the initialization operation in the Firebase Firestore or any other remote data source
        val studentCourseProgressCollection = databaseService.db.collection("studentCourseProgress")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseProgressDocument = studentCourseProgressCollection.document(studentCourseDocumentId).get().await()

        if (!studentCourseProgressDocument.exists()) {
            // If the document does not exist, initialize the student's course progress
            val newStudentCourseProgress = hashMapOf(
                "studentId" to studentId,
                "courseId" to courseId,
                "lastCompletedLesson" to 0, // The student has not completed any lessons yet
                "progressDate" to Timestamp.now() // Use the current time as the progress date
            )

            studentCourseProgressCollection.document(studentCourseDocumentId).set(newStudentCourseProgress).await()
        } else {
            throw Exception("Student's course progress has already been initialized")
        }
    }
}
