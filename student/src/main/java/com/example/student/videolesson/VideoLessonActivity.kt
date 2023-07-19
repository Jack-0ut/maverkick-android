package com.example.student.videolesson

import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.student.R
import com.example.student.adapters.EmojiAdapter
import com.example.student.databinding.ActivityVideoLessonBinding
import com.example.tasks.ExerciseDialogFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the VideoActivity class. It is responsible for displaying the video lessons to the students.
 * The activity allows students to interact with the video by pausing and playing it.
 * They can also ask questions while the video is playing by clicking on the 'ASK' icon.
 */
@AndroidEntryPoint
class VideoLessonActivity : AppCompatActivity(), ExerciseDialogFragment.ExerciseDialogListener {
    private lateinit var binding: ActivityVideoLessonBinding
    private lateinit var player: ExoPlayer
    private val viewModel: VideoLessonViewModel by viewModels()

    // Passed parameters
    private lateinit var lessonId: String
    private lateinit var videoUri: String
    private lateinit var transcription: String
    private lateinit var title: String
    private lateinit var courseId: String

    // Initialize the AudioFocusRequest and AudioManager variables
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var audioManager: AudioManager

    // Initialize the playback control variables
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    private val emojis = listOf("🤩", "😊", "😐", "😕", "😡")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityVideoLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get extras from intent
        lessonId = intent.getStringExtra("lessonId") ?: ""
        videoUri = intent.getStringExtra("videoUri") ?: ""
        transcription = intent.getStringExtra("transcription") ?: ""
        title = intent.getStringExtra("title") ?: ""
        courseId = intent.getStringExtra("courseId") ?: ""

        binding.videoTitle.text = title

        // Prevent the screen from timing out while the video is playing
        binding.videoView.keepScreenOn = true

        // Set click listener for the "ASK" icon to pause the video and show the AskQuestionDialogFragment
        binding.chatButton.setOnClickListener {
            player.pause()

            val dialogFragment = AskQuestionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("transcription", transcription)
                    putString("lessonId", lessonId)
                }
            }
            dialogFragment.show(supportFragmentManager, "AskQuestionDialogFragment")
        }

        // emoji reaction button
        binding.reactionButton.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.emoji_picker)
            // Add these lines to make the dialog window background transparent
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val gridView = dialog.findViewById<GridView>(R.id.grid_view)
            val adapter = EmojiAdapter(this, R.layout.item_emoji, emojis) { selectedEmoji, position ->
                dialog.dismiss()
                val rating = 5 - position
                viewModel.setLessonRating(rating)
            }
            gridView.adapter = adapter

            dialog.show()
        }
        // Initialize the AudioManager and AudioFocusRequest
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> player.playWhenReady = true
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.playWhenReady = false
                    AudioManager.AUDIOFOCUS_LOSS -> player.playWhenReady = false
                }
            }
            .build()
    }

    /** Initializes the ExoPlayer instance and sets the video source URL. */
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // Check if the ExerciseDialogFragment is already added
                    val existingFragment = supportFragmentManager.findFragmentByTag("ExerciseDialogFragment")
                    if (existingFragment == null) {
                        // Create an Intent with an action named "LESSON_COMPLETED_ACTION"
                        val intent = Intent("LESSON_COMPLETED_ACTION")

                        // Add additional data to the intent
                        intent.putExtra("lessonId", lessonId)
                        intent.putExtra("courseId", courseId)

                        // Send the broadcast
                        LocalBroadcastManager.getInstance(this@VideoLessonActivity).sendBroadcast(intent)

                        val exerciseDialogFragment = ExerciseDialogFragment.newInstance(courseId, lessonId)

                        // Show the ExerciseDialogFragment
                        exerciseDialogFragment.show(supportFragmentManager, "ExerciseDialogFragment")

                        // Update the rating in the database
                        viewModel.updateRatings(courseId, {}, {})
                    }
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
        // Checks the orientation of the screen
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                binding.videoTitle.visibility = View.GONE
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                binding.videoTitle.visibility = View.VISIBLE
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }

    /** Releases the ExoPlayer when it's not needed to free up resources.*/
    private fun releasePlayer() {
        if (::player.isInitialized) {
            playbackPosition = player.currentPosition
            currentWindow = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            audioManager.abandonAudioFocusRequest(focusRequest)
            player.release()
        }
    }

    // Override lifecycle methods to handle player initialization and release
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || !::player.isInitialized) {
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

    /** When student completes all of the tasks, just finish the activity **/
    override fun onExercisesCompleted() {
        finish()
    }
}

