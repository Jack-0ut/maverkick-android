package com.maverkick.text_lesson.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.maverkick.data.repositories.TextCourseRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import com.maverkick.text_lesson.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class CheckCourseGenerationProgressWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val textCourseRepository: TextCourseRepository,
    private val sharedPrefManager: SharedPrefManager
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val TAG = "CourseProgressWorker"
        private const val MAX_RETRIES = 20
    }

    override suspend fun doWork(): Result {
        val courseId = inputData.getString("courseId") ?: throw CourseIdMissingException()
        setForegroundAsync(createForegroundInfo(courseId))

        for (i in 1..MAX_RETRIES) {
            val progress = textCourseRepository.checkCourseProgress(courseId)
            Log.d(TAG, "Current progress: $progress")

            when (progress) {
                "finished" -> return handleFinished(courseId)
                "error", "null" -> return handleError(courseId)
                else -> delay(2 * 60 * 1000) // Wait for 2 minutes
            }
        }
        return handleTimeout()
    }

    private fun createForegroundInfo(courseId: String): ForegroundInfo {
        val id = "personalized_course_generation_channel"
        val title = "Course Generation"
        val description = "Generating course content for $courseId"

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, title, importance).apply {
            setDescription(description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Set visibility on lock screen
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_build)
            .setChannelId(id)
            .setOngoing(true)  // Set the notification as ongoing
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Set visibility on lock screen
            .setProgress(0, 0, true)
            .build()

        return ForegroundInfo(getNotificationId(courseId), notification)
    }

    private fun getNotificationId(courseId: String): Int {
        return courseId.hashCode()
    }

    private fun canShowNotification(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private suspend fun handleFinished(courseId: String): Result {
        Log.d("CourseGeneratedEvent", "Handling finished for courseId: $courseId")

        Log.d("CourseGeneratedEvent", "Start handling for courseId: $courseId")

        // notify the system, that we need to update the list of courses
        sharedPrefManager.setNeedsRefresh(true)

        // Cancel the foreground notification
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(getNotificationId(courseId))

        if (canShowNotification()) {
            Log.d("HandleFinished", "Can show notification.")

            notificationManager.createNotificationChannel(createSuccessNotificationChannel())

            val notification = NotificationCompat.Builder(context, "course_generation_success_channel")
                .setContentTitle("Course Generation Completed")
                .setContentText("Your course has been successfully generated.")
                .setSmallIcon(com.maverkick.common.R.drawable.ic_notification_success)
                .build()

            delay(5000)

            // Adding an offset to the notification ID for the success notification
            notificationManager.notify(getNotificationId(courseId) + 1, notification)
            Log.d("HandleFinished", "Notification sent for courseId: $courseId")
        } else {
            Log.d("HandleFinished", "Cannot show notification.")
        }

        return Result.success()
    }

    private suspend fun handleError(courseId: String): Result {
        // Cancel the foreground notification
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(getNotificationId(courseId))

        if (canShowNotification()) {
            notificationManager.createNotificationChannel(createErrorNotificationChannel())

            val notification = NotificationCompat.Builder(context, "course_generation_error_channel")
                .setContentTitle("Course Generation Failed")
                .setContentText("An error occurred, so try it once more.")
                .setSmallIcon(com.maverkick.common.R.drawable.ic_notification_error)
                .build()

            delay(5000)
            notificationManager.notify(getNotificationId(courseId), notification)
        }
        return Result.failure()
    }


    private fun createSuccessNotificationChannel(): NotificationChannel {
        val id = "course_generation_success_channel"
        val title = "Course Generation Success"
        val description = "Notifications for successful course generation"
        val importance = NotificationManager.IMPORTANCE_HIGH

        return NotificationChannel(id, title, importance).apply {
            setDescription(description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
    }

    private fun createErrorNotificationChannel(): NotificationChannel {
        val id = "course_generation_error_channel"
        val title = "Course Generation Error"
        val description = "Notifications for errors during course generation"
        val importance = NotificationManager.IMPORTANCE_HIGH

        return NotificationChannel(id, title, importance).apply {
            setDescription(description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
    }

    private fun handleTimeout(): Result {
        return Result.failure()
    }

    class CourseIdMissingException : Exception("courseId is missing in inputData")
}
