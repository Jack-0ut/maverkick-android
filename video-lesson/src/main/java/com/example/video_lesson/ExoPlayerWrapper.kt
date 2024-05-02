package com.example.video_lesson

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player


/**
 * A wrapper around the ExoPlayer library, simplifying its functionality for easier use.
 *
 * This class serves as an intermediary between the application and ExoPlayer. It implements
 * the [SimplePlayer] interface, providing a simplified approach to video playback using ExoPlayer.
 *
 * @property context The context used to build the ExoPlayer instance.
 */
class ExoPlayerWrapper(private val context: Context) : VideoPlayerInterface {

    /**
     * Lazily-initialized instance of ExoPlayer.
     */
    private val exoPlayer: ExoPlayer by lazy { ExoPlayer.Builder(context).build() }

    /**
     * Provides direct access to the underlying ExoPlayer instance.
     */
    val playerInstance: ExoPlayer
        get() = exoPlayer

    /**
     * Indicates whether the media should automatically start playing when ready.
     */
    override var playWhenReady: Boolean
        get() = exoPlayer.playWhenReady
        set(value) { exoPlayer.playWhenReady = value }

    /**
     * Current window index being played by ExoPlayer.
     */
    override var currentWindow: Int
        get() = exoPlayer.currentWindowIndex
        set(value) {
            exoPlayer.seekTo(value, exoPlayer.currentPosition)
        }

    /**
     * Current position in the playback window in milliseconds.
     */
    override var currentPosition: Long
        get() = exoPlayer.currentPosition
        set(value) {
            exoPlayer.seekTo(exoPlayer.currentWindowIndex, value)
        }

    /**
     * Prepares the ExoPlayer for playback.
     */
    override fun prepare() {
        exoPlayer.prepare()
    }

    /**
     * Releases resources associated with the ExoPlayer instance.
     */
    override fun release() {
        exoPlayer.release()
    }

    /**
     * Sets the media item to be played by ExoPlayer.
     *
     * @param url The URL of the media to be played.
     */
    override fun setMediaItem(url: String) {
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        exoPlayer.setMediaItem(mediaItem)
    }

    /**
     * Seeks to a specific position in a specific window for playback.
     *
     * @param window The index of the window.
     * @param position The position within the window in milliseconds.
     */
    override fun seekTo(window: Int, position: Long) {
        exoPlayer.seekTo(window, position)
    }

    /**
     * Adds a listener to be notified when video playback reaches the end.
     *
     * @param listener A lambda to be invoked when playback ends.
     */
    override fun addEndOfVideoListener(listener: () -> Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    listener.invoke()
                }
            }
        })
    }
}
