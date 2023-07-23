package com.example.videoplayer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.Util

/** Abstraction of the ExoPlayer with defined auto-rotation and resume playing **/
abstract class AbstractVideoActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var audioManager: AudioManager

    protected abstract val videoUrl: String
    protected abstract fun onPlayerEnd()
    protected abstract fun initializeBinding()
    protected abstract fun setPlayerView(player: ExoPlayer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    AudioManager.AUDIOFOCUS_GAIN -> player?.playWhenReady = true
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player?.playWhenReady = false
                    AudioManager.AUDIOFOCUS_LOSS -> player?.playWhenReady = false
                }
            }
            .build()

        initializeBinding()
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

            val videoUri = Uri.parse(videoUrl)
            val mediaItem = MediaItem.fromUri(videoUri)

            setMediaItem(mediaItem)
            seekTo(currentWindow, playbackPosition)
            playWhenReady = playWhenReady
            prepare()
        }

        setPlayerView(player!!)

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player!!.prepare()
        }
    }

    protected open fun releasePlayer() {
        player?.let { player ->
            playbackPosition = player.currentPosition
            currentWindow = player.currentWindowIndex
            playWhenReady = player.playWhenReady
            player.release()
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
        if ((Util.SDK_INT < 24 || player == null)) {
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
        outState.putInt("currentWindow", currentWindow)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentWindow = savedInstanceState.getInt("currentWindow")
        playbackPosition = savedInstanceState.getLong("playbackPosition")
        playWhenReady = savedInstanceState.getBoolean("playWhenReady")
    }
}
