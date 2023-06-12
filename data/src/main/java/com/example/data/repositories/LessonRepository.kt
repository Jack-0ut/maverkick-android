package com.example.data.repositories

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.example.data.IDatabaseService
import com.example.data.models.Lesson
import com.example.data.models.Student
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import javax.inject.Inject

/**
 * Class responsible for the Lesson objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class LessonRepository @Inject constructor(
    private val databaseService: IDatabaseService,
    private val firebaseStorage: FirebaseStorage)
{

    /** Add new lesson to the database **/
    fun addLesson(courseID: String, lesson: Lesson, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        databaseService.db.collection("courses").document(courseID).collection("lessons").add(lesson)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    /** Get the lessons for the course **/
    fun getCourseLessons(courseID: String, onSuccess: (List<Lesson>) -> Unit, onFailure: (Exception) -> Unit) {
        val lessonsRef = databaseService.db.collection("courses").document(courseID).collection("lessons")

        lessonsRef.get()
            .addOnSuccessListener { documents ->
                val lessons = documents.map { it.toObject(Lesson::class.java) }
                onSuccess(lessons)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /** Upload the video to the firestore **/
    fun uploadVideo(courseId: String, videoUri: Uri,
                    onProgressListener: (Int) -> Unit,
                    onSuccessListener: (Pair<String, String>) -> Unit,
                    onFailureListener: (Exception) -> Unit) {
        Log.d("SexRepository", "Attempting to upload video $videoUri to course $courseId")
        // Generate the unique id for our lesson
        val lessonId = UUID.randomUUID().toString()
        // Put the video into the defined directory
        val storageRef = firebaseStorage.getReference("/courses/$courseId/lessons/$lessonId")

        // Upload video to Firebase Storage
        val uploadTask = storageRef.putFile(videoUri)

        // Monitor state of upload
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = ((100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
            onProgressListener(progress)
        }.addOnPausedListener {
            Log.d("Upload", "Upload is paused")
        }.addOnFailureListener { exception ->
            // Handle unsuccessful uploads
            Log.d("Upload", "Upload failed")
            onFailureListener(exception)
        }.addOnSuccessListener {
            // Handle successful uploads on complete
            Log.d("Upload", "Upload completed successfully")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccessListener(Pair(lessonId, uri.toString()))
            }
        }
    }


    /** After we uploaded the video, we're adding the url of that video  to the lesson document **/
    fun updateFirestoreWithVideoUrl(courseId: String, lessonId: String, downloadUrl: String, onSuccessListener: () -> Unit, onFailureListener: (Exception) -> Unit) {
        val lessonDocument = databaseService.db.collection("courses").document(courseId).collection("lessons").document(lessonId)

        lessonDocument.update("videoUrl", downloadUrl)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                onSuccessListener()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                onFailureListener(e)
            }
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
