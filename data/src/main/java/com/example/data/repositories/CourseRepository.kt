package com.example.data.repositories

import android.net.Uri
import android.util.Log
import com.algolia.search.client.Index
import com.algolia.search.helper.deserialize
import com.algolia.search.model.search.Query
import com.example.data.IDatabaseService
import com.example.data.models.Course
import com.example.data.models.FirebaseCourse
import com.example.data.models.SearchCourseHit
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Class responsible for the Course objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class CourseRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val firebaseStorage: FirebaseStorage,
    private val algoliaIndex: Index
) {
    /** Add new Course **/
    fun addCourse(course: Course, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseCourse = course.toFirebaseCourse()
        databaseService.db.collection("courses")
            .add(firebaseCourse)
            .addOnSuccessListener { documentReference ->
                val courseId = documentReference.id
                onSuccess(courseId)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /** Method that searches courses for given query**/
    suspend fun searchCourses(query: String): List<SearchCourseHit> {
        val response = algoliaIndex.run { search(Query(query)) }
        return response.hits.deserialize(SearchCourseHit.serializer())
    }

    /**  Get Course object by it's id and provide real-time updates for the course **/
    fun getCourseById(courseId: String, onSuccess: (Course?) -> Unit, onFailure: (Exception) -> Unit): ListenerRegistration {
        return databaseService.db.collection("courses").document(courseId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    onFailure(firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Log DocumentSnapshot object
                    Log.d("CourseRepository", "Fetched DocumentSnapshot: $documentSnapshot")

                    val firebaseCourse = documentSnapshot.toObject(FirebaseCourse::class.java)

                    // Log FirebaseCourse object
                    Log.d("CourseRepository", "Fetched FirebaseCourse: $firebaseCourse")

                    val course = firebaseCourse?.toCourse(documentSnapshot.id)

                    // Log Course object
                    Log.d("CourseRepository", "Converted to Course: $course")

                    onSuccess(course)
                }
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

                val tasks = courseIds.map { courseId ->
                    databaseService.db.collection("courses").document(courseId)
                        .get()
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
                    val courses = documents.mapNotNull { document ->
                        val firebaseCourse = document.toObject(FirebaseCourse::class.java)
                        firebaseCourse?.toCourse(document.id)
                    }

                    onSuccess(courses)
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Get the list of courses for the teacher(author) with given Id*/
    suspend fun getTeacherCourses(teacherID: String): List<Course> {
        // Make the suspend function cancellable
        return suspendCancellableCoroutine { cont ->
            databaseService.db.collection("courses")
                .whereEqualTo("teacherId", teacherID)
                .get()
                .addOnSuccessListener { documents ->
                    val courses = documents.mapNotNull { document ->
                        document.toObject(FirebaseCourse::class.java).toCourse(document.id)
                    }
                    cont.resume(courses)
                }
                .addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
        }
    }

    suspend fun getPublishedCourses(): List<Course> = suspendCoroutine { continuation ->
        databaseService.db.collection("courses")
            .whereEqualTo("published", true)
            .get()
            .addOnSuccessListener { documents ->
                val courses = documents.mapNotNull { document ->
                    document.toObject(FirebaseCourse::class.java).toCourse(document.id)
                }
                continuation.resume(courses)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }



    /** Save new poster to the firebase storage under the courseId name */
    fun updatePoster(courseId:String, uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = firebaseStorage.reference
        val posterRef = storageRef.child("posters/$courseId")

        val uploadTask = posterRef.putFile(uri)
        uploadTask.addOnFailureListener { exception ->
            // Handle unsuccessful uploads
            onFailure(exception)
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            posterRef.downloadUrl.addOnSuccessListener { downloadUri ->
                updateCoursePoster(courseId, downloadUri.toString(),
                    onSuccess = { onSuccess(downloadUri.toString()) },
                    onFailure = { exception -> onFailure(exception) }
                )
            }
        }
    }

    /** Update the url of the poster inside the firestore courses collection **/
    private fun updateCoursePoster(courseId:String, posterUrl: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val courseRef = databaseService.db.collection("courses").document(courseId)
        courseRef
            .update("poster", posterUrl)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Make course active - accessible for everyone **/
    fun activateCourse(courseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val courseRef = databaseService.db.collection("courses").document(courseId)
        courseRef.update("published", true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    /** Recommend the list of courses for student, taking into account interests**/
    suspend fun getRecommendedCourses(interestsList:List<String>): List<SearchCourseHit> {
        val response = algoliaIndex.run { search(Query(interestsList.toString())) }
        return response.hits.deserialize(SearchCourseHit.serializer())
    }

    /** Remove course with given id **/
    fun removeCourse(courseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses").document(courseId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

}
