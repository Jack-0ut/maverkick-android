package com.maverkick.videoplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util

abstract class AbstractVideoActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var audioManager: AudioManager

    protected abstract val videoUrl: String
    protected abstract fun onPlayerEnd()
    protected abstract fun initializeBinding()
    protected abstract fun setPlayerView(player: ExoPlayer)

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
        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlayerEnd()
                    }
                }
            })
            setMediaItem(MediaItem.fromUri(videoUrl))
            seekTo(currentWindow, playbackPosition)
            prepare()
        }

        setPlayerView(player!!)
        player!!.playWhenReady = playWhenReady

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player!!.prepare()
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
