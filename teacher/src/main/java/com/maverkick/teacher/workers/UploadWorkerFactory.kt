package com.maverkick.teacher.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.maverkick.data.repositories.LessonRepository
import javax.inject.Inject

/** Abstraction for the UploadWorker, which allows to inject dependencies
 * directly into the UploadWorker class
 **/
class UploadWorkerFactory @Inject constructor(private val lessonRepository: LessonRepository) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = UploadWorker(appContext, workerParameters, lessonRepository)
}