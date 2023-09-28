package com.maverkick.teacher.addlesson

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.snackbar.Snackbar
import com.maverkick.teacher.databinding.ActivityAddLessonBinding
import com.maverkick.teacher.workers.UploadWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Final activity in adding new lesson, when teacher see the video
 * and maybe could do some editing on it, but right now
 * it's just preview and some buttons
 **/
@AndroidEntryPoint
class AddLessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLessonBinding
    private val viewModel: AddVideoLessonViewModel by viewModels()
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.main_tone_color)

        // initialize the video player
        player = ExoPlayer.Builder(this).build().apply {
            binding.videoPreview.player = this
            playWhenReady = true // auto play
        }

        // set the values in the viewModel
        intent.apply {
            getStringExtra("COURSE_ID")?.let(viewModel::setCourseId)
            getStringExtra("VIDEO_URI")?.let { viewModel.selectVideo(Uri.parse(it)) }
            getIntExtra("VIDEO_DURATION", 0).let(viewModel::setVideoDuration)
            getStringExtra("LANGUAGE_CODE")?.let(viewModel::setCourseLanguage)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.videoUri.collect { videoUri ->
                        videoUri?.let {
                            player?.setMediaItem(MediaItem.fromUri(it))
                            player?.prepare()
                        }
                    }
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            val data = workDataOf(
                "courseId" to viewModel.courseId.value,
                "videoUri" to viewModel.videoUri.value.toString(),
                "languageCode" to viewModel.getBCP47LanguageTag(viewModel.language.value),
                "videoDuration" to viewModel.videoDuration.value
            )

            val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)

            Snackbar.make(binding.root, "Start Video Uploading", Snackbar.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)
    }
}
