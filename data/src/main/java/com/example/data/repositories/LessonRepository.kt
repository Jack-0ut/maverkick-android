package com.example.data.repositories

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.data.IDatabaseService
import com.example.data.api.Api
import com.example.data.api.TranscriptionRequest
import com.example.data.models.Lesson
import com.example.data.models.LessonFirebase
import com.example.data.models.Student
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * Class responsible for the Lesson objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 * @param firebaseStorage - the object of our storage
 **/
class LessonRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val firebaseStorage: FirebaseStorage,
    private val api: Api
    ) {

    /** Add new lesson to the database **/
    fun addLesson(
        courseID: String,
        lesson: Lesson,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        databaseService.db.collection("courses").document(courseID).collection("lessons")
            .add(lesson)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /** Get the lessons for the course **/
    fun getCourseLessons(
        courseID: String,
        onSuccess: (List<Lesson>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val lessonsRef =
            databaseService.db.collection("courses").document(courseID).collection("lessons")
        lessonsRef.get()
            .addOnSuccessListener { documents ->
                val lessons = documents.mapNotNull { document ->
                    try {
                        val firebaseLesson = document.toObject(LessonFirebase::class.java)
                        // Convert firebaseLesson to Lesson only if videoUrl and transcription are not null
                        if (firebaseLesson.videoUrl != null && firebaseLesson.transcription != null) {
                            firebaseLesson.toLesson(document.id)
                        } else {
                            null // Ignore documents that can't be converted to Lesson
                        }
                    } catch (e: Exception) {
                        null // Ignore documents that can't be converted to LessonFirebase
                    }
                }
                onSuccess(lessons)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Upload the video to the firestore **/
    suspend fun uploadVideo(courseId: String, videoUri: Uri): Pair<String, String> =
        suspendCancellableCoroutine { continuation ->
            // Generate the unique id for our lesson
            val lessonId = UUID.randomUUID().toString()
            // Put the video into the defined directory
            val storageRef = firebaseStorage.getReference("/courses/$courseId/lessons/$lessonId")

            // Upload video to Firebase Storage
            val uploadTask = storageRef.putFile(videoUri)

            // Monitor state of upload
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress =
                    ((100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                // onProgressListener(progress)
            }.addOnPausedListener {
                Log.d("Upload", "Upload is paused")
            }.addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                Log.d("Upload", "Upload failed")
                continuation.resumeWithException(exception)
            }.addOnSuccessListener {
                // Handle successful uploads on complete
                Log.d("Upload", "Upload completed successfully")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resume(Pair(lessonId, uri.toString()))
                }
            }
        }

    /** After we uploaded the video, we're adding the url of that video to the lesson document **/
    suspend fun updateFirestoreWithVideoUrl(courseId: String, lessonId: String, downloadUrl: String) {
        suspendCancellableCoroutine { continuation ->
            val lessonDocument = databaseService.db.collection("courses").document(courseId).collection("lessons").document(lessonId)

            lessonDocument.set(hashMapOf("videoUrl" to downloadUrl), SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    continuation.resume(Unit)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    continuation.resumeWithException(e)
                }
        }
    }

    /** Transcribe the video by calling the api endpoint **/
    suspend fun transcribeVideo(courseId: String, lessonId: String, filePath: String, languageCode: String):
            Response<String> {
        val request = TranscriptionRequest(courseId, lessonId, filePath, languageCode)
        val response = api.transcribeVideo(request)

        if (response.isSuccessful) {
            Log.d("LessonRepository", "Transcription successful: ${response.body()}")
        } else {
            Log.d("LessonRepository", "Transcription failed with response code: ${response.code()}")
        }

        return response
    }

    /** Get the list of today's lessons for the student **/
    fun getTodayLessons(studentID: String, onSuccess: (List<Lesson>) -> Unit, onFailure: (Exception) -> Unit) {
        // Fetch the student document
        databaseService.db.collection("students").document(studentID).get()
            .addOnSuccessListener { studentDoc ->
                val student = studentDoc.toObject(Student::class.java)
                val dailyStudyTime = student?.dailyStudyTimeMinutes ?: 0

                // Fetch the active courses for the student
                databaseService.db.collection("studentCourses").whereEqualTo("studentId", studentID).whereEqualTo("active", true).get()
                    .addOnSuccessListener { courseDocs ->
                        val courseIds = courseDocs.mapNotNull { it.getString("courseId") }
                        val lessonQueues = mutableListOf<Queue<Lesson>>()
                        var coursesProcessed = 0

                        // Fetch the lessons for each active course
                        for (courseID in courseIds) {
                            databaseService.db.collection("courses").document(courseID).collection("lessons").orderBy("lesson_order").get()
                                .addOnSuccessListener { lessonDocs ->
                                    val lessons = lessonDocs.mapNotNull { it.toObject(Lesson::class.java) }
                                    lessonQueues.add(LinkedList(lessons))
                                    coursesProcessed++

                                    // Round-robin through the lesson queues once all courses have been processed
                                    if (coursesProcessed == courseIds.size) {
                                        val todayLessons = mutableListOf<Lesson>()
                                        var totalLessonTime = 0
                                        var i = 0

                                        while (totalLessonTime < dailyStudyTime && lessonQueues.any { it.isNotEmpty() }) {
                                            val queue = lessonQueues[i % lessonQueues.size]
                                            if (queue.isNotEmpty()) {
                                                val lesson = queue.poll()
                                                val lessonTime = lesson.length
                                                // Add the lesson to the list if there is enough remaining study time
                                                if (totalLessonTime + lessonTime <= (dailyStudyTime * 60)) {
                                                    todayLessons.add(lesson)
                                                    totalLessonTime += lessonTime
                                                }
                                            }
                                            i++
                                        }
                                        onSuccess(todayLessons)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    onFailure(exception)
                                    return@addOnFailureListener
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                        return@addOnFailureListener
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
