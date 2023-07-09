package com.example.teacher.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.data.repositories.LessonRepository
import com.example.teacher.R
import javax.inject.Inject
import kotlin.random.Random


/**
 * Worker that is responsible for the uploading of the video-lesson,
 * storing the data in the lesson collection and generating the title for the lesson
 **/
@HiltWorker
class UploadWorker @Inject constructor(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val lessonRepository: LessonRepository
): CoroutineWorker(context, workerParameters) {

    /** Method that execute the chain of works in the background to transcribe the video
     * and generate the title for it **/
    override suspend fun doWork(): Result {
        val courseId = inputData.getString("courseId")
        val videoUri = inputData.getString("videoUri")?.let { Uri.parse(it) }
        val languageCode = inputData.getString("languageCode")
        val videoDuration = inputData.getInt("videoDuration",0)

        if (courseId == null || videoUri == null || languageCode == null) {
            return Result.failure()
        }

        // display the uploading to the user
        setForegroundAsync(startForegroundService())

        return try {
            // Upload the video
            val (lessonId, downloadUrl) = lessonRepository.uploadVideo(courseId, videoUri)

            // Update Firestore with video URL and duration
            lessonRepository.updateFirestoreWithVideoUrl(courseId, lessonId, downloadUrl, videoDuration)

            // Transcribe video
            lessonRepository.transcribeVideo(courseId, lessonId, downloadUrl, languageCode)

            Result.success()
        } catch (e: Exception) {
            // If there's an exception, return failure
            return Result.failure()
        }
    }


    /** Display the uploading process to the user**/
    private fun startForegroundService(): ForegroundInfo {
        val id = "upload_channel"
        val title = "Upload Service"
        val description = "Uploading Video Lesson"

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, title, importance).apply {
            setDescription(description)
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_done)
            .setChannelId(id)
            .setProgress(0, 0, true)
            .build()

        return ForegroundInfo(Random.nextInt(), notification)
    }
}

