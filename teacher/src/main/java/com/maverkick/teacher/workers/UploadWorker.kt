package com.maverkick.teacher.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.maverkick.data.repositories.VideoLessonRepository
import com.maverkick.teacher.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.random.Random


/**
 * Worker that is responsible for the uploading of the video-lesson,
 * storing the data in the lesson collection and generating the title for the lesson
 **/
@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val videoLessonRepository: VideoLessonRepository
) : CoroutineWorker(context, workerParameters) {

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

        setForegroundAsync(startForegroundService())

        return try {
            // Upload the video
            val (lessonId, downloadUrl) = videoLessonRepository.uploadVideo(courseId, videoUri)

            // Update Firestore with video URL and duration
            videoLessonRepository.updateFirestoreWithVideoUrl(courseId, lessonId, downloadUrl, videoDuration)

            // Transcribe video
            videoLessonRepository.transcribeVideo(courseId, lessonId, downloadUrl, languageCode)

            Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }


    /** Display the uploading process to the user**/
    private fun startForegroundService(): ForegroundInfo {
        val id = "upload_channel"
        val title = "Upload Video Lesson"
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
            .setSmallIcon(R.drawable.ic_upload)
            .setChannelId(id)
            .setProgress(0, 0, true)
            .build()

        return ForegroundInfo(Random.nextInt(), notification)
    }
}

