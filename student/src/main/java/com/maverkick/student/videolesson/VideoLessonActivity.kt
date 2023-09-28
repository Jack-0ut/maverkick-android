package com.maverkick.student.videolesson

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.GridView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.example.chat_helper.AskQuestionDialogFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import com.maverkick.data.models.CourseType
import com.maverkick.student.R
import com.maverkick.student.adapters.EmojiAdapter
import com.maverkick.student.databinding.ActivityVideoLessonBinding
import com.maverkick.tasks.ExerciseActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the VideoActivity class. It is responsible for displaying the video lessons to the students.
 * The activity allows students to interact with the video by pausing and playing it.
 * They can also ask questions while the video is playing by clicking on the 'ASK' icon.
 */
@AndroidEntryPoint
class VideoLessonActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoLessonBinding
    private val player: ExoPlayer by lazy { ExoPlayer.Builder(this).build() }
    private val viewModel: VideoLessonViewModel by viewModels()

    // Passed parameters
    private lateinit var lessonId: String
    private lateinit var videoUri: String
    private lateinit var transcription: String
    private lateinit var title: String
    private lateinit var courseId: String

    private val emojis = listOf("🤩", "😊", "😐", "😕", "😡")

    // Audio focus
    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build()
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(audioAttributes)
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> player.playWhenReady = true
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS -> player.playWhenReady = false
            }
        }
        .build()

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.main_tone_color)

        binding = ActivityVideoLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.extras?.let {
            lessonId = it.getString("lessonId", "")
            videoUri = it.getString("videoUri", "")
            transcription = it.getString("transcription", "")
            title = it.getString("title", "")
            courseId = it.getString("courseId", "")
        }

        binding.apply {
            videoTitle.text = title
            videoView.keepScreenOn = true

            chatButton.setOnClickListener { onChatButtonClick() }
            reactionButton.setOnClickListener { onReactionButtonClick() }
        }

        initializePlayer()
    }

    private fun onChatButtonClick() {
        player.pause()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)

        val dialogFragment = AskQuestionDialogFragment().apply {
            arguments = bundleOf(
                "courseId" to courseId,
                "transcription" to transcription,
                "lessonId" to lessonId
            )
            dismissListener = { window.statusBarColor = ContextCompat.getColor(this@VideoLessonActivity, com.maverkick.common.R.color.main_tone_color) }
        }
        dialogFragment.show(supportFragmentManager, "AskQuestionDialogFragment")
    }

    private fun onReactionButtonClick() {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.emoji_picker)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<GridView>(R.id.grid_view).apply {
                adapter = EmojiAdapter(this@VideoLessonActivity, R.layout.item_emoji, emojis) { _, position ->
                    dismiss()
                    viewModel.setLessonRating(5 - position)
                }
            }
        }
        dialog.show()
    }

    private fun initializePlayer() {
        binding.videoView.player = player

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    val intent = ExerciseActivity.newIntent(this@VideoLessonActivity, courseId, lessonId, CourseType.VIDEO)
                    startActivity(intent)
                    finish()
                    // Update the rating in the database
                    viewModel.updateRatings(courseId, {}, {})
                }
            }
        })

        val videoUri = Uri.parse(this.videoUri)
        val mediaItem = MediaItem.fromUri(videoUri)

        player.setMediaItem(mediaItem)
        player.playWhenReady = playWhenReady
        player.seekTo(currentWindow, playbackPosition)

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.prepare()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.videoTitle.visibility = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            View.GONE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            View.VISIBLE
        }
    }

    private fun releasePlayer() {
        playbackPosition = player.currentPosition
        currentWindow = player.currentMediaItemIndex
        playWhenReady = player.playWhenReady
        audioManager.abandonAudioFocusRequest(focusRequest)
        player.release()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)
    }
}
