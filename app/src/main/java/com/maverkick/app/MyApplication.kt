package com.maverkick.app

import android.app.Application
import androidx.work.Configuration
import com.maverkick.teacher.workers.UploadWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


/**
 * Starting point of the application, where initialization happening
 **/
@HiltAndroidApp
class MyApplication : Application(),Configuration.Provider{
    @Inject lateinit var uploadWorkerFactory: UploadWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(uploadWorkerFactory)
            .build()
}