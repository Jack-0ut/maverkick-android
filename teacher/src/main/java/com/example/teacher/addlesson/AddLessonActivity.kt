package com.example.teacher.addlesson

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.teacher.databinding.ActivityAddLessonBinding
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
                            }
                        }
                    }
                }
            }
        }

        binding.buttonSave.setOnClickListener {
            viewModel.uploadVideo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
