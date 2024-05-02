package com.example.video_lesson

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.util.Util

/** Abstract VideoPlayer, which incorporates basic features like play,pause,rotation, etc..**/
abstract class AbstractVideoActivity : AppCompatActivity() {

    protected var player: VideoPlayerInterface? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var audioManager: AudioManager

    protected abstract val videoUrl: String
    protected abstract fun onPlayerEnd()
    protected abstract fun initializeBinding()
    protected abstract fun setPlayerView(player: VideoPlayerInterface)
    protected abstract fun createPlayerInstance(): VideoPlayerInterface

    companion object {
        private const val KEY_WINDOW = "key_window"
        private const val KEY_POSITION = "key_position"
        private const val KEY_PLAY_WHEN_READY = "key_play_when_ready"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build())
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener { focusChange ->
                handleAudioFocusChange(focusChange)
            }
        }.build()

        initializeBinding()

        savedInstanceState?.let {
            currentWindow = it.getInt(KEY_WINDOW)
            playbackPosition = it.getLong(KEY_POSITION)
            playWhenReady = it.getBoolean(KEY_PLAY_WHEN_READY)
        }
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> player?.playWhenReady = true
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS -> player?.playWhenReady = false
        }
    }

    protected open fun initializePlayer() {
        if (player == null) {
            player = createPlayerInstance()
            setPlayerView(player!!)
        }

        player?.setMediaItem(videoUrl)
        player?.seekTo(currentWindow, playbackPosition)
        player?.playWhenReady = playWhenReady

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player?.prepare()
        }

        player?.addEndOfVideoListener {
            onPlayerEnd()
        }
    }

    protected open fun releasePlayer() {
        player?.apply {
            playbackPosition = currentPosition
            playWhenReady = playWhenReady
            release()
        }
        player = null
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || player == null) {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_WINDOW, currentWindow)
        outState.putLong(KEY_POSITION, playbackPosition)
        outState.putBoolean(KEY_PLAY_WHEN_READY, playWhenReady)
    }
}
