package com.maverkick.data.repositories

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.maverkick.data.IDatabaseService
import com.maverkick.data.api.ApiResult
import com.maverkick.data.api.CourseCreationApi
import com.maverkick.data.api.CourseGenerationRequest
import com.maverkick.data.api.CourseGenerationResponse
import com.maverkick.data.models.FirebasePersonalizedTextCourse
import com.maverkick.data.models.PersonalizedTextCourse
import retrofit2.Response
import javax.inject.Inject

/**
 * Class responsible for the GeneratedTextCourses interaction with the
 * database, reading and controlling the generation process
 * @param databaseService - database to which we're connecting
 **/
class PersonalizedTextCourseRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val courseCreationApi: CourseCreationApi
) {
    /**  Get TextCourse object by it's id and provide real-time updates for the textCourse **/
    fun getGeneratedTextCourseById(textCourseId: String, onSuccess: (PersonalizedTextCourse?) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("generatedCourses").document(textCourseId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val firebasePersonalizedTextCourse = documentSnapshot.toObject(FirebasePersonalizedTextCourse::class.java)
                    val textCourse = firebasePersonalizedTextCourse?.toCourse(documentSnapshot.id)
                    onSuccess(textCourse)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Get the list of TextCourse objects for the list of courseIds **/
    fun getGeneratedTextCoursesByIds(courseIds: List<String>, onSuccess: (List<PersonalizedTextCourse>) -> Unit, onFailure: (Exception) -> Unit) {
        val tasks = courseIds.map { courseId ->
            databaseService.db.collection("generatedCourses").document(courseId)
                .get()
        }
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { courseDocuments ->
            val courses = courseDocuments.mapNotNull { document ->
                val firebasePersonalizedTextCourse = document.toObject(FirebasePersonalizedTextCourse::class.java)
                firebasePersonalizedTextCourse?.toCourse(document.id) //
            }
            onSuccess(courses)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun checkCourseProgress(courseId: String): String? {
        val courseDocument = databaseService.db.collection("generatedCourses").document(courseId)
        return try {
            val snapshot = Tasks.await(courseDocument.get())

            if (!snapshot.exists()) {
                return "null"
            }

            val status = snapshot.getString("status")
            status
        } catch (e: Exception) {
            "error"
        }
    }

    /** Create personalized course with the text prompt **/
    suspend fun generateCourse(userId: String, coursePrompt: String, language: String): ApiResult<CourseGenerationResponse> {
        val response = courseCreationApi.generateCourse(CourseGenerationRequest(userId, coursePrompt, language))
        return if (response.isSuccessful) {
            ApiResult(body = response.body(), isSuccess = true)
        } else {
            ApiResult(body = null, isSuccess = false, errorMessage = response.message())
        }
    }

}
