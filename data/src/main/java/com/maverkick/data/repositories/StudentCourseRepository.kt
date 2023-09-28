package com.maverkick.data.repositories

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.CourseType
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

    suspend fun enrollStudent(studentId: String, courseId: String, courseType: CourseType): Boolean {
        val studentCoursesCollection = databaseService.db.collection("studentCourses")
        val studentCourseDocumentId = "${studentId}_${courseId}"
        val studentCourseDocument = studentCoursesCollection.document(studentCourseDocumentId).get().await()

        return if (!studentCourseDocument.exists()) {
            val newStudentCourse = hashMapOf(
                "studentId" to studentId,
                "courseId" to courseId,
                "courseType" to courseType.name,
                "enrollmentDate" to Timestamp.now(),
                "active" to true
            )
            studentCoursesCollection.document(studentCourseDocumentId).set(newStudentCourse).await()
            true
        } else {
            false
        }
    }

    suspend fun initStudentCourseProgress(studentId: String, courseId: String, courseType: CourseType): Boolean {
        val studentCourseProgressCollection = databaseService.db.collection("studentCourseProgress")

        val studentCourseDocumentId = "${studentId}_${courseId}"
        val studentCourseProgressDocument = studentCourseProgressCollection.document(studentCourseDocumentId).get().await()

        return if (!studentCourseProgressDocument.exists()) {
            val newStudentCourseProgress = hashMapOf(
                "studentId" to studentId,
                "courseId" to courseId,
                "courseType" to courseType.name,
                "lastCompletedLesson" to 0,
                "progressDate" to Timestamp.now()
            )

            studentCourseProgressCollection.document(studentCourseDocumentId).set(newStudentCourseProgress).await()
            true
        } else {
            false
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
