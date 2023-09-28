package com.maverkick.data.repositories

import com.google.firebase.firestore.FieldValue
import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.FirebaseStudent
import com.maverkick.data.models.Student
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Class responsible for the Student objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class StudentRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new Student to the database and if everything is OK, return the student **/
    suspend fun addStudent(userId: String, age: Int, dailyStudyTimeMinutes: Int, interests: List<String>): Result<Student> {
        // Create the FirebaseStudent object
        val firebaseStudent = FirebaseStudent(age, dailyStudyTimeMinutes, interests)
        // Add the Student to the database
        return try {
            databaseService.db.collection("students").document(userId).set(firebaseStudent).await()
            // Convert the FirebaseStudent to a Student, including the userId
            val student = firebaseStudent.toStudent(userId)
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update the daily study time **/
    suspend fun updateDailyStudyTime(userId: String, newDailyStudyTime: Int): Result<Boolean> {
        return try {
            databaseService.db.collection("students").document(userId).update("dailyStudyTimeMinutes", newDailyStudyTime).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get a Student by their user ID **/
    suspend fun getStudentById(userId: String): Result<Student> {
        return suspendCoroutine { continuation ->
            databaseService.db.collection("students")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val interests: List<String> = (document.get("interests") as? List<*>)?.map { it.toString() } ?: listOf()
                        val enrolledVideoCourses: List<String> = (document.get("enrolledVideoCourses") as? List<*>)?.map { it.toString() } ?: listOf()
                        val enrolledTextCourses: List<String> = (document.get("enrolledTextCourses") as? List<*>)?.map { it.toString() } ?: listOf()
                        val createdTextCourses: List<String> = (document.get("createdTextCourses") as? List<*>)?.map { it.toString() } ?: listOf()

                        val firebaseStudent = FirebaseStudent(
                            age = document.getLong("age")?.toInt() ?: 0,
                            dailyStudyTimeMinutes = document.getLong("dailyStudyTimeMinutes")?.toInt() ?: 0,
                            interests = interests,
                            bricksCollected = document.getLong("bricksCollected")?.toInt() ?: 0,
                            enrolledVideoCourses = enrolledVideoCourses,
                            enrolledTextCourses = enrolledTextCourses,
                            createdTextCourses = createdTextCourses,
                            courseGenerationTries = document.getLong("courseGenerationTries")?.toInt() ?: 0
                        )
                        val student = firebaseStudent.toStudent(userId)
                        continuation.resume(Result.success(student))
                    } else {
                        continuation.resume(Result.failure(Exception("No such student")))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /** Increment the number of bricks collected by 1 every time called **/
    suspend fun incrementCollectedBricks(userId: String): Result<Boolean> {
        return try {
            databaseService.db.collection("students").document(userId)
                .update("bricksCollected", FieldValue.increment(1))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Add course to the list of enrolled **/
    suspend fun addCourseToEnrolled(userId: String, courseId: String, courseType: CourseType): Result<Boolean> {
        return try {
            val studentDocumentRef = databaseService.db.collection("students").document(userId)
            val field = if (courseType == CourseType.VIDEO) "enrolledVideoCourses" else "enrolledTextCourses"

            studentDocumentRef.update(field, FieldValue.arrayUnion(courseId)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Remove particular course from the list **/
    suspend fun removeCourseFromEnrolled(userId: String, courseId: String, courseType: CourseType): Result<Boolean> {
        return try {
            val studentDocumentRef = databaseService.db.collection("students").document(userId)
            val field = if (courseType == CourseType.VIDEO) "enrolledVideoCourses" else "enrolledTextCourses"

            studentDocumentRef.update(field, FieldValue.arrayRemove(courseId)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /** Get video course IDs for the student with given Id **/
    suspend fun getVideoCourses(studentID: String): Result<List<String>> {
        return try {
            val studentDocument = databaseService.db.collection("students").document(studentID)
                .get()
                .await()

            val enrolledVideoCourses = studentDocument.get("enrolledVideoCourses") as? List<String> ?: emptyList()

            Result.success(enrolledVideoCourses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get text course IDs for the student with given Id **/
    suspend fun getTextCourses(studentID: String): Result<List<String>> {
        return try {
            val studentDocument = databaseService.db.collection("students").document(studentID)
                .get()
                .await()

            val enrolledTextCourses = studentDocument.get("enrolledTextCourses") as? List<String> ?: emptyList()

            Result.success(enrolledTextCourses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get the list of IDs of generated text courses that the student has not enrolled in **/
    suspend fun getUnenrolledGeneratedTextCourseIds(studentID: String): Result<List<String>> {
        return try {
            // Fetch the student's document
            val studentDocument = databaseService.db.collection("students").document(studentID)
                .get()
                .await()

            // Fetch all the text courses that were generated (both enrolled and unenrolled)
            val allGeneratedTextCourses = studentDocument.get("generatedTextCourses") as? List<String> ?: emptyList()

            // Fetch enrolled text courses for the student
            val enrolledTextCourses = studentDocument.get("enrolledTextCourses") as? List<String> ?: emptyList()

            // Filter out the enrolled text courses to find unenrolled ones
            val unenrolledTextCourseIds = allGeneratedTextCourses.filter { it !in enrolledTextCourses }

            Result.success(unenrolledTextCourseIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get the number of course generation tries for a student with a given Id **/
    suspend fun getCourseGenerationTries(studentID: String): Result<Int> {
        return try {
            val studentDocument = databaseService.db.collection("students").document(studentID)
                .get()
                .await()

            val courseGenerationTries = studentDocument.getLong("courseGenerationTries")?.toInt() ?: 0

            Result.success(courseGenerationTries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
