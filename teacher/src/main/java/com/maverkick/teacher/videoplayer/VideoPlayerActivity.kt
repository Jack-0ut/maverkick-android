package com.maverkick.teacher.videoplayer

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.maverkick.videoplayer.AbstractVideoActivity
import com.maverkick.videoplayer.databinding.ActivityVideoAbstractBinding

/**Video Player that plays the particular lesson in the course.
 * Called from the EditCourseFragment
 **/
class VideoPlayerActivity : AbstractVideoActivity() {
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


    override fun onPlayerEnd() {
        // Handle end of the video here.
        // This method will be called when the video finishes playing.
    }

    // Implement initializeBinding in your subclass
    override fun initializeBinding() {
        _binding = ActivityVideoAbstractBinding.inflate(layoutInflater)
        setContentView(_binding.root)
    }

    // Implement setPlayerView in your subclass
    override fun setPlayerView(player: ExoPlayer) {
        _binding.playerView.player = player
    }

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)
    }

    companion object {
        const val EXTRA_VIDEO_URL = "extra_video_url"
    }
}
