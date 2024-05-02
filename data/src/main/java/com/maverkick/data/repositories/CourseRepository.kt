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
import com.maverkick.data.models.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
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

    /**
     * Get a Course object by its id and provide real-time updates for the course.
     * The function retrieves the course from the 'courses' collection in Firestore based on the provided courseId.
     * It converts the Firestore document first to a Firebase class (FirebaseVideoCourse or FirebaseTextCourse),
     * and then to the respective model class (VideoCourse or GeneralTextCourse).
     *
     * @param courseId The id of the course to be fetched.
     * @param onSuccess A callback function that is invoked with the Course object when the operation is successful.
     * @param onFailure A callback function that is invoked with an Exception when the operation fails.
     * @return A ListenerRegistration object that can be used to remove the listener when it is no longer needed.
     */
    fun getCourseByIdRealTime(courseId: String, onSuccess: (Course?) -> Unit, onFailure: (Exception) -> Unit): ListenerRegistration {
        return databaseService.db.collection("courses").document(courseId)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    onFailure(firebaseFirestoreException)
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val course: Course? = when (documentSnapshot.getString("courseType") ?: "") {
                        CourseType.VIDEO.toString() -> {
                            val firebaseVideoCourse = documentSnapshot.toObject(FirebaseVideoCourse::class.java)
                            firebaseVideoCourse?.toCourse(documentSnapshot.id)
                        }
                        CourseType.TEXT.toString() -> {
                            val firebaseTextCourse = documentSnapshot.toObject(FirebaseTextCourse::class.java)
                            firebaseTextCourse?.toCourse(documentSnapshot.id)
                        }
                        else -> null
                    }
                    onSuccess(course)
                }
            }
    }

    /**
     * Get a Course object by its id.
     * The function retrieves the course from the 'courses' collection in Firestore based on the provided courseId.
     * It converts the Firestore document first to a Firebase class (FirebaseVideoCourse or FirebaseTextCourse),
     * and then to the respective model class (VideoCourse or GeneralTextCourse).
     *
     * @param courseId The id of the course to be fetched.
     * @param onSuccess A callback function that is invoked with the Course object when the operation is successful.
     * @param onFailure A callback function that is invoked with an Exception when the operation fails.
     */
    fun getCourseById(courseId: String, onSuccess: (Course?) -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses").document(courseId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val course: Course? = when (documentSnapshot.getString("courseType") ?: "") {
                        CourseType.VIDEO.toString() -> {
                            val firebaseVideoCourse = documentSnapshot.toObject(FirebaseVideoCourse::class.java)
                            firebaseVideoCourse?.toCourse(documentSnapshot.id)
                        }
                        CourseType.TEXT.toString() -> {
                            val firebaseTextCourse = documentSnapshot.toObject(FirebaseTextCourse::class.java)
                            firebaseTextCourse?.toCourse(documentSnapshot.id)
                        }
                        else -> null
                    }
                    onSuccess(course)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Get the list of Course objects (VideoCourse or GeneralTextCourse) for the list of courseIds.
     * The function iterates over the list of courseIds, fetching each course from the 'courses' collection in Firestore.
     * Then, it converts each Firestore document first to a Firebase class (FirebaseVideoCourse or FirebaseTextCourse),
     * and then to the respective model class (VideoCourse or GeneralTextCourse).
     *
     * @param courseIds A list of courseIds for which to fetch the Course objects.
     * @param onSuccess A callback function that is invoked with a list of Course objects when the operation is successful.
     * @param onFailure A callback function that is invoked with an Exception when the operation fails.
     */
    fun getCoursesByIds(courseIds: List<String>, onSuccess: (List<Course>) -> Unit, onFailure: (Exception) -> Unit) {
        val tasks = courseIds.filter { it.isNotEmpty() }.map { courseId ->
            databaseService.db.collection("courses").document(courseId)
                .get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { courseDocuments ->
            val courses = courseDocuments.mapNotNull { document ->
                when (document.getString("courseType") ?: "") {
                    CourseType.VIDEO.toString() -> {
                        val firebaseVideoCourse = document.toObject(FirebaseVideoCourse::class.java)
                        firebaseVideoCourse?.toCourse(document.id)
                    }
                    CourseType.TEXT.toString() -> {
                        val firebaseTextCourse = document.toObject(FirebaseTextCourse::class.java)
                        firebaseTextCourse?.toCourse(document.id)
                    }
                    else -> null
                }
            }

            onSuccess(courses)
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    /** Get the list of courses for the teacher(author) with given Id*/
    suspend fun getTeacherCourses(teacherID: String): List<Course> {
        return suspendCancellableCoroutine { cont ->
            databaseService.db.collection("courses")
                .whereEqualTo("authorId", teacherID)
                .get()
                .addOnSuccessListener { documents ->
                    val courses = documents.mapNotNull { document ->
                        when (document.getString("courseType") ?: "") {
                            CourseType.VIDEO.toString() -> {
                                val firebaseVideoCourse = document.toObject(FirebaseVideoCourse::class.java)
                                firebaseVideoCourse.toCourse(document.id)
                            }
                            CourseType.TEXT.toString() -> {
                                val firebaseTextCourse = document.toObject(FirebaseTextCourse::class.java)
                                firebaseTextCourse.toCourse(document.id)
                            }
                            else -> null
                        }
                    }
                    cont.resume(courses)
                }
                .addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
        }
    }

    /** Get the list of published courses, which we use in the Gallery **/
    suspend fun getPublishedCourses(): List<Course> = suspendCoroutine { continuation ->
        databaseService.db.collection("courses")
            .whereEqualTo("published", true)
            .get()
            .addOnSuccessListener { documents ->
                val courses = documents.mapNotNull { document ->
                    when (document.getString("courseType") ?: "") {
                        CourseType.VIDEO.toString() -> {
                            val firebaseVideoCourse = document.toObject(FirebaseVideoCourse::class.java)
                            firebaseVideoCourse.toCourse(document.id)
                        }
                        CourseType.TEXT.toString() -> {
                            val firebaseTextCourse = document.toObject(FirebaseTextCourse::class.java)
                            firebaseTextCourse.toCourse(document.id)
                        }
                        else -> null
                    }
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

    /** Return number of the lessons in the course **/
    suspend fun getNumberOfLessonsForCourse(courseId: String): Int {
        val courseDocument =
            databaseService.db.collection("courses").document(courseId).get().await()
        return (courseDocument["lessonCount"] as? Long)?.toInt() ?: 0
    }


}
