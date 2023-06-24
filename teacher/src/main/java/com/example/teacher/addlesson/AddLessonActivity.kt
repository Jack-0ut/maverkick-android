package com.example.teacher.addlesson

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.teacher.databinding.ActivityAddLessonBinding
import com.example.teacher.workers.UploadWorker
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
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

        // initialize the video player
        player = ExoPlayer.Builder(this).build()
        binding.videoPreview.player = player

        // put the values into the viewModel
        intent.getStringExtra("COURSE_ID")?.let { courseId ->
            viewModel.setCourseId(courseId)
        }

        intent.getStringExtra("VIDEO_URI")?.let { uriString ->
            viewModel.selectVideo(Uri.parse(uriString))
        }

        intent.getIntExtra("VIDEO_DURATION", 0).let { duration ->
            viewModel.setVideoDuration(duration)
        }

        intent.getStringExtra("LANGUAGE_CODE")?.let{ languageCode ->
            viewModel.setCourseLanguage(languageCode)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.videoUri.collect { videoUri ->
                        videoUri?.let {
                            val mediaItem = MediaItem.fromUri(videoUri)
                            player?.setMediaItem(mediaItem)
                            player?.prepare()
                            player?.playWhenReady = true // auto play
                        }
                    }
                }

                launch {
                    viewModel.uploadProgress.collect { progress ->
                        // Update the progress bar with the current progress
                        binding.progressBar.progress = progress
                    }
                }

                launch {
                    viewModel.uploadStatus.collect { status ->
                        when(status) {
                            is UploadStatus.Success -> {
                                binding.progressBar.visibility = View.GONE
                                // Show a success message or perform other actions after successful upload
                            }
                            is UploadStatus.Failure -> {
                                binding.progressBar.visibility = View.GONE
                                // Show an error message based on status.exception
                            }
                            is UploadStatus.InProgress -> {
                                binding.progressBar.visibility = View.VISIBLE
                                //binding.buttonSave.visibility = View.GONE
                            }
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

            Toast.makeText(this, "Upload started", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
