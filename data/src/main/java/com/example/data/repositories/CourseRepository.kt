package com.example.data.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.example.data.IDatabaseService
import com.example.data.models.Course
import com.example.data.models.FirebaseCourse
import javax.inject.Inject

/**
 * Class responsible for the Course objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class CourseRepository @Inject constructor(private val databaseService: IDatabaseService) {
    /** Add new Course **/
    fun addCourse(course: Course, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseCourse = course.toFirebaseCourse()
        databaseService.db.collection("courses")
            .add(firebaseCourse)
            .addOnSuccessListener { documentReference ->
                val courseId = documentReference.id
                Log.d(TAG, "DocumentSnapshot added with ID: $courseId")
                onSuccess(courseId)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                onFailure(e)
            }
    }

    /** Method that searches courses for given query**/
    fun searchCourses(query: String, onSuccess: (List<Course>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses")
            .whereEqualTo("name", query)
            .get()
            .addOnSuccessListener { documents ->
                val courses = documents.mapNotNull { it.toObject(FirebaseCourse::class.java)
                    .toCourse(it.id) }
                onSuccess(courses)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Remove course with given id **/
    fun removeCourse(courseId: String) {
        databaseService.db.collection("courses").document(courseId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
    }

    /** Get Course object by it's id **/
    fun getCourseById(courseId: String, onSuccess: (Course?) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val firebaseCourse = documentSnapshot.toObject(FirebaseCourse::class.java)
                val course = firebaseCourse?.toCourse(documentSnapshot.id)
                onSuccess(course)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Get the list of courses for the student with given Id*/
    fun getStudentCourses(studentID: String, onSuccess: (List<Course>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("studentCourses")
            .whereEqualTo("studentId", studentID)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { documents ->
                val courseIds = documents.mapNotNull { it.getString("courseId") }

                val courses = mutableListOf<Course>()
                courseIds.forEach { courseId ->
                    databaseService.db.collection("courses").document(courseId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val firebaseCourse = documentSnapshot.toObject(FirebaseCourse::class.java)
                            val course = firebaseCourse?.toCourse(documentSnapshot.id)
                            if (course != null) {
                                courses.add(course)
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }
                onSuccess(courses)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Convert the Firebase course into our Course object*/

}
