package com.maverkick.data.repositories

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.maverkick.data.IDatabaseService
import com.maverkick.data.api.CourseCreationApi
import com.maverkick.data.api.CourseGenerationRequest
import com.maverkick.data.api.CourseGenerationResponse
import com.maverkick.data.models.FirebaseTextCourse
import com.maverkick.data.models.TextCourse
import retrofit2.Response
import javax.inject.Inject

/**
 * Class responsible for the TextCourse objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class TextCourseRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val courseCreationApi: CourseCreationApi
) {
    /**  Get TextCourse object by it's id and provide real-time updates for the textCourse **/
    fun getTextCourseById(textCourseId: String, onSuccess: (TextCourse?) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("textCourses").document(textCourseId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val firebaseTextCourse = documentSnapshot.toObject(FirebaseTextCourse::class.java)
                    val textCourse = firebaseTextCourse?.toCourse(documentSnapshot.id)
                    onSuccess(textCourse)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Get the list of TextCourse objects for the list of courseIds **/
    fun getTextCoursesByIds(courseIds: List<String>, onSuccess: (List<TextCourse>) -> Unit, onFailure: (Exception) -> Unit) {
        val tasks = courseIds.map { courseId ->
            databaseService.db.collection("textCourses").document(courseId)
                .get()
        }
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { courseDocuments ->
            val courses = courseDocuments.mapNotNull { document ->
                val firebaseTextCourse = document.toObject(FirebaseTextCourse::class.java)
                firebaseTextCourse?.toCourse(document.id) //
            }
            onSuccess(courses)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun checkCourseProgress(courseId: String): String? {
        val courseDocument = databaseService.db.collection("textCourses").document(courseId)

        return try {
            val snapshot = Tasks.await(courseDocument.get())

            if (!snapshot.exists()) {
                // If the document itself doesn't exist
                return "null"
            }

            val status = snapshot.getString("status")
            status
        } catch (e: Exception) {
            "error"
        }
    }


    /** Create personalized course with the text prompt **/
    suspend fun generateCourse(userId: String, coursePrompt: String, language: String): Response<CourseGenerationResponse> {
        return courseCreationApi.generateCourse(CourseGenerationRequest(userId, coursePrompt, language))
    }

}
