package com.example.student.videolesson


import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.student.databinding.ActivityVideoLessonBinding
import com.example.student.videolesson.exercise.ExerciseDialogFragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util

/**
 * This is the VideoActivity class. It is responsible for displaying the video lessons to the students.
 * The activity allows students to interact with the video by pausing and playing it.
 * They can also ask questions while the video is playing by clicking on the 'ASK' icon.
 */
class VideoLessonActivity : AppCompatActivity() {
    // Initialize the View Binding and ExoPlayer variables
    private lateinit var binding: ActivityVideoLessonBinding
    private lateinit var player: ExoPlayer

    // Initialize the AudioFocusRequest and AudioManager variables
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var audioManager: AudioManager

    // Initialize the playback control variables
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityVideoLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent the screen from timing out while the video is playing
        binding.videoView.keepScreenOn = true

        // Set click listener for the video view to pause/play the video
        binding.videoView.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

        // Set click listener for the "ASK" icon to pause the video and show the AskQuestionDialogFragment
        binding.askIcon.setOnClickListener {
            player.pause()
            AskQuestionDialogFragment().show(supportFragmentManager, "AskQuestionDialogFragment")
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


    /**
     * Initializes the ExoPlayer instance and sets the video source URL.
     */
    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.videoView.player = player

        // navigate to the exercise fragment, when video ends
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    ExerciseDialogFragment().show(supportFragmentManager, "ExerciseDialogFragment")
                }
            }
        })

        val videoUri = Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        val mediaItem = MediaItem.fromUri(videoUri)

        player.setMediaItem(mediaItem)
        player.playWhenReady = playWhenReady
        player.seekTo(currentWindow, playbackPosition)

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.prepare()
        }
    }
    /**
     * Releases the ExoPlayer when it's not needed to free up resources.
     */
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
}
