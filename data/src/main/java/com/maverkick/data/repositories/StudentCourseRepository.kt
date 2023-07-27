package com.maverkick.data.repositories

import com.maverkick.data.IDatabaseService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Describes interaction between student and it's courses:
 * enrolling, dropping,re-enrolling, capturing progress
 **/
class StudentCourseRepository @Inject constructor(private val databaseService: IDatabaseService){

    /** Add the lesson to the list of completed for a given student's course**/
    fun addLessonToCompleted(studentCourseId: String, lessonId: String) {
        val studentCourseRef = databaseService.db.collection("studentCourseProgress").document(studentCourseId)
        databaseService.db.runTransaction { transaction ->
            transaction.update(studentCourseRef, "completedLessons", FieldValue.arrayUnion(lessonId))
            transaction.update(studentCourseRef, "lastCompletedLesson", FieldValue.increment(1))
        }
    }
    /** Add new document to the studentCourses collection **/
    suspend fun enrollStudent(studentId: String, courseId: String): Boolean {
        val studentCoursesCollection = databaseService.db.collection("studentCourses")
        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseDocument = studentCoursesCollection.document(studentCourseDocumentId).get().await()

        return if (!studentCourseDocument.exists()) {
            val newStudentCourse = hashMapOf(
                "studentId" to studentId,
                "courseId" to courseId,
                "enrollmentDate" to Timestamp.now(),
                "active" to true
            )

            studentCoursesCollection.document(studentCourseDocumentId).set(newStudentCourse).await()
            true
        } else {
            false
        }
    }

    /** Add new document to the studentCourseProgress collection **/
    suspend fun initStudentCourseProgress(studentId: String, courseId: String): Boolean {
        val studentCourseProgressCollection = databaseService.db.collection("studentCourseProgress")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseProgressDocument = studentCourseProgressCollection.document(studentCourseDocumentId).get().await()

        return if (!studentCourseProgressDocument.exists()) {
            val newStudentCourseProgress = hashMapOf(
                "studentId" to studentId,
                "courseId" to courseId,
                "lastCompletedLesson" to 0,
                "progressDate" to Timestamp.now()
            )

            studentCourseProgressCollection.document(studentCourseDocumentId).set(newStudentCourseProgress).await()
            true
        } else {
            false
        }
    }


    /** Fetches the student course progress **/
    suspend fun getStudentCourseProgress(studentId: String, courseId: String): Int {
        val studentCourseProgressCollection = databaseService.db.collection("studentCourseProgress")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseProgressDocument = studentCourseProgressCollection.document(studentCourseDocumentId).get().await()

        return if (studentCourseProgressDocument.exists()) {
            // If the document exists, return the lastCompletedLesson field
            studentCourseProgressDocument.get("lastCompletedLesson") as Int
        } else {
            // If the document does not exist, return 0 as the default value
            0
        }
    }

    /** Withdraw student from the particular course **/
    suspend fun withdrawStudent(studentId: String, courseId: String) {
        // Perform the withdrawal operation in the Firebase Firestore or any other remote data source
        val studentCoursesCollection = databaseService.db.collection("studentCourses")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseDocument = studentCoursesCollection.document(studentCourseDocumentId).get().await()

        if (studentCourseDocument.exists()) {
            // If the document exists, withdraw the student from the course by setting "active" to false
            studentCoursesCollection.document(studentCourseDocumentId).update("active", false).await()
        } else {
            throw Exception("Student is not enrolled in the course")
        }
    }

    /** Check if the record for the student and course exists, meaning that student has been enrolled in the course **/
    suspend fun isStudentEverBeenEnrolled(studentId: String, courseId: String): Boolean {
        val studentCoursesCollection = databaseService.db.collection("studentCourses")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseDocument = studentCoursesCollection.document(studentCourseDocumentId).get().await()

        return studentCourseDocument.exists()
    }

    /** Re-enroll student in the particular course **/
    suspend fun reEnrollStudent(studentId: String, courseId: String) {
        // Perform the re-enrollment operation in the Firebase Firestore or any other remote data source
        val studentCoursesCollection = databaseService.db.collection("studentCourses")

        val studentCourseDocumentId = "${studentId}_$courseId"
        val studentCourseDocument = studentCoursesCollection.document(studentCourseDocumentId)

        // Update the 'active' field to true to re-enroll the student
        studentCourseDocument.update("active", true).await()
    }


}
