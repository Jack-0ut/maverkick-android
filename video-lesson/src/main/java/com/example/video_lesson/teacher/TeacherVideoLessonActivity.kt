package com.example.video_lesson.teacher

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.video_lesson.AbstractVideoActivity
import com.example.video_lesson.ExoPlayerWrapper
import com.example.video_lesson.VideoPlayerInterface
import com.example.video_lesson.databinding.ActivityVideoAbstractBinding

/** Video Player that plays the particular lesson in the course for the Teacher **/
class TeacherVideoLessonActivity : AbstractVideoActivity() {
    private lateinit var _binding: ActivityVideoAbstractBinding

    // Implement the abstract property from the base class
    override val videoUrl: String
        get() = intent.getStringExtra(EXTRA_VIDEO_URL) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVideoAbstractBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.black)
    }

    override fun onPlayerEnd() {}

    override fun initializeBinding() {
        _binding = ActivityVideoAbstractBinding.inflate(layoutInflater)
        setContentView(_binding.root)
    }

    override fun setPlayerView(player: VideoPlayerInterface) {
        val exoSimplePlayer = player as? ExoPlayerWrapper
        _binding.playerView.player = exoSimplePlayer?.playerInstance
    }

    override fun createPlayerInstance(): VideoPlayerInterface {
        return ExoPlayerWrapper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)
    }

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
    }
}
