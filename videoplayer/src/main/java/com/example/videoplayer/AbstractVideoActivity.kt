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
    private lateinit var player: ExoPlayer
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    // AudioManager and AudioFocusRequest variables
    private lateinit var focusRequest: AudioFocusRequest
    private lateinit var audioManager: AudioManager

    protected abstract val videoUrl: String

    protected abstract fun onPlayerEnd()

    protected abstract fun initializeBinding()

    protected abstract fun setPlayerView(player: ExoPlayer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AudioManager and AudioFocusRequest
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

        initializeBinding()
    }

    protected open fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        setPlayerView(player)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onPlayerEnd()
                }
            }
        })

        val videoUri = Uri.parse(videoUrl)
        val mediaItem = MediaItem.fromUri(videoUri)

        player.setMediaItem(mediaItem)
        player.seekTo(currentWindow, playbackPosition)
        player.playWhenReady = playWhenReady

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.prepare()
        }
    }

    protected open fun releasePlayer() {
        if (::player.isInitialized) {
            playbackPosition = player.currentPosition
            currentWindow = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            audioManager.abandonAudioFocusRequest(focusRequest)
            player.release()
        }
    }

    // Override lifecycle methods
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || !::player.isInitialized)) {
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
