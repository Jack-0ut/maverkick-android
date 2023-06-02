package com.example.data.repositories

import android.content.ContentValues.TAG
import android.util.Log
import com.example.data.IDatabaseService
import com.example.data.models.Course
import javax.inject.Inject

/**
 * Class responsible for the Course objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class CourseRepository @Inject constructor(private val databaseService: IDatabaseService) {

    /** Add new Course object to the database **/
    fun addCourse(course: Course) {
        databaseService.db.collection("courses")
            .add(course)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    /** Search courses in the database **/
    fun searchCourses(query: String, onSuccess: (List<Course>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses")
            .whereEqualTo("name", query)
            .get()
            .addOnSuccessListener { documents ->
                val courses = documents.mapNotNull { it.toObject(Course::class.java) }
                onSuccess(courses)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Remove a course from the database **/
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

    /** Get all of the courses a Student is currently studying **/
    fun getStudentCourses(studentID: String, onSuccess: (List<Course>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("studentCourses")
            .whereEqualTo("student_id", studentID)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { documents ->
                val courseIds = documents.mapNotNull { it.getString("course_id") }

                val courses = mutableListOf<Course>()
                courseIds.forEach { courseId ->
                    databaseService.db.collection("courses").document(courseId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val course = documentSnapshot.toObject(Course::class.java)
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

}
