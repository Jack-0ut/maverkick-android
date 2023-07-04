package com.example.data.repositories

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.data.IDatabaseService
import com.example.data.api.LessonApi
import com.example.data.api.TranscriptionRequest
import com.example.data.models.Lesson
import com.example.data.models.LessonFirebase
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Class responsible for the Lesson objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 * @param firebaseStorage - the object of our storage
 * @param lessonApi - the api that transcribe the video and stores it to the database
 **/
class LessonRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val firebaseStorage: FirebaseStorage,
    private val lessonApi: LessonApi
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
    suspend fun updateFirestoreWithVideoUrl(courseId: String, lessonId: String, downloadUrl: String,videoDuration: Int) {
        suspendCancellableCoroutine { continuation ->
            val lessonDocument = databaseService.db.collection("courses").document(courseId).collection("lessons").document(lessonId)

            lessonDocument.set(hashMapOf("videoUrl" to downloadUrl, "duration" to videoDuration), SetOptions.merge())
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
        val response = lessonApi.transcribeVideo(request)

        if (response.isSuccessful) {
            Log.d("LessonRepository", "Transcription successful: ${response.body()}")
        } else {
            Log.d("LessonRepository", "Transcription failed with response code: ${response.code()}")
        }

        return response
    }

    /** Get the list of today's lessons for the student **/
    suspend fun getTodayLessons(studentId: String, dailyStudyTimeMinutes: Int): List<Lesson> {
        val dailyStudyTime = dailyStudyTimeMinutes * 60
        Log.d("getTodayLessons", "Target study time: $dailyStudyTime seconds")

        // Fetch the active courses for the student
        val courseDocs = suspendCoroutine<QuerySnapshot> { continuation ->
            databaseService.db.collection("studentCourses")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("getTodayLessons", "Fetched ${querySnapshot.size()} active courses for student $studentId")
                    continuation.resume(querySnapshot)
                }
                .addOnFailureListener { exception ->
                    Log.e("getTodayLessons", "Failed to fetch active courses", exception)
                    continuation.resumeWithException(exception)
                }
        }

        val courseIds = courseDocs.documents.mapNotNull { it.getString("courseId") }
        val lessonQueues = mutableListOf<Queue<Lesson>>()

        // Fetch the lessons for each active course
        for (courseID in courseIds) {
            val lessonDocs = suspendCoroutine<QuerySnapshot> { continuation ->
                databaseService.db.collection("courses").document(courseID)
                    .collection("lessons")
                    .orderBy("lesson_order")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        Log.d("getTodayLessons", "Fetched ${querySnapshot.size()} lessons for course $courseID")
                        continuation.resume(querySnapshot)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("getTodayLessons", "Failed to fetch lessons for course $courseID", exception)
                        continuation.resumeWithException(exception)
                    }
            }

            val lessons = lessonDocs.documents.mapNotNull { it.toObject(Lesson::class.java) }
            lessonQueues.add(LinkedList(lessons))
        }

        // Round-robin through the lesson queues
        val todayLessons = mutableListOf<Lesson>()
        var totalLessonTime = 0
        var i = 0

        while (totalLessonTime < dailyStudyTime && lessonQueues.any { it.isNotEmpty() }) {
            val queue = lessonQueues[i % lessonQueues.size]
            if (queue.isNotEmpty()) {
                val lesson = queue.poll()
                val lessonTime = lesson.duration
                // Add the lesson to the list if there is enough remaining study time
                if (totalLessonTime + lessonTime <= dailyStudyTime) {
                    todayLessons.add(lesson)
                    totalLessonTime += lessonTime
                    Log.d("getTodayLessons", "Added lesson ${lesson.lessonId} to today's lessons. Current total lesson time: $totalLessonTime seconds")
                }
            }
            i++
        }

        Log.d("getTodayLessons", "Prepared ${todayLessons.size} lessons for today")

        return todayLessons
    }


}
