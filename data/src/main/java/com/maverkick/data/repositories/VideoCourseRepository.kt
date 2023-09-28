package com.maverkick.data.repositories

import android.net.Uri
import com.algolia.search.client.Index
import com.algolia.search.helper.deserialize
import com.algolia.search.model.search.Query
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.FirebaseVideoCourse
import com.maverkick.data.models.SearchCourseHit
import com.maverkick.data.models.VideoCourse
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
class VideoCourseRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val firebaseStorage: FirebaseStorage,
    private val algoliaIndex: Index
) {
    /** Add new Course **/
    fun addCourse(videoCourse: VideoCourse, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseCourse = videoCourse.toFirebaseCourse()
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
    fun getCourseById(courseId: String, onSuccess: (VideoCourse?) -> Unit, onFailure: (Exception) -> Unit): ListenerRegistration {
        return databaseService.db.collection("courses").document(courseId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    onFailure(firebaseFirestoreException)
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val firebaseVideoCourse = documentSnapshot.toObject(FirebaseVideoCourse::class.java)
                    val course = firebaseVideoCourse?.toCourse(documentSnapshot.id)
                    onSuccess(course)
                }
            }
    }

    /** Get the list of courses for the student with given Id*/
    fun getStudentVideoCourses(studentID: String, onSuccess: (List<VideoCourse>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("studentCourses")
            .whereEqualTo("studentId", studentID)
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { studentDocuments ->
                val courseIds = studentDocuments.mapNotNull { it.getString("courseId") }

                val tasks = courseIds.map { courseId ->
                    databaseService.db.collection("courses").document(courseId)
                        .get()
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { courseDocuments ->
                    val courses = courseDocuments.mapNotNull { document ->
                        val firebaseVideoCourse = document.toObject(FirebaseVideoCourse::class.java)
                        firebaseVideoCourse?.toCourse(document.id)
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

    /** Get the list of VideoCourse objects for the list of courseIds **/
    fun getVideoCoursesByIds(courseIds: List<String>, onSuccess: (List<VideoCourse>) -> Unit, onFailure: (Exception) -> Unit) {
        val tasks = courseIds.map { courseId ->
            databaseService.db.collection("courses").document(courseId)
                .get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { courseDocuments ->
            val courses = courseDocuments.mapNotNull { document ->
                val firebaseVideoCourse = document.toObject(FirebaseVideoCourse::class.java)
                firebaseVideoCourse?.toCourse(document.id) // Assuming `toCourse` is a method that translates FirebaseVideoCourse to Course
            }

            onSuccess(courses)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    /** Get the list of courses for the teacher(author) with given Id*/
    suspend fun getTeacherCourses(teacherID: String): List<VideoCourse> {
        // Make the suspend function cancellable
        return suspendCancellableCoroutine { cont ->
            databaseService.db.collection("courses")
                .whereEqualTo("teacherId", teacherID)
                .get()
                .addOnSuccessListener { documents ->
                    val courses = documents.mapNotNull { document ->
                        document.toObject(FirebaseVideoCourse::class.java).toCourse(document.id)
                    }
                    cont.resume(courses)
                }
                .addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
        }
    }
    /** Get the list of published courses, which we use in the Gallery **/
    suspend fun getPublishedCourses(): List<VideoCourse> = suspendCoroutine { continuation ->
        databaseService.db.collection("courses")
            .whereEqualTo("published", true)
            .get()
            .addOnSuccessListener { documents ->
                val courses = documents.mapNotNull { document ->
                    document.toObject(FirebaseVideoCourse::class.java).toCourse(document.id)
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
