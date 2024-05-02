package com.example.video_lesson

/**
 * Represents a simple video player interface that abstracts the underlying player's details,
 * providing a unified approach for video playback.
 */
interface VideoPlayerInterface {

    /**
     * Determines if the video should start playing immediately once it's ready.
     */
    var playWhenReady: Boolean

    /**
     * Refers to the index of the currently active media item (e.g., in a playlist scenario).
     */
    var currentWindow: Int

    /**
     * Gets or sets the current playback position within the active media item in milliseconds.
     */
    var currentPosition: Long

    /**
     * Prepares the player for playback, setting it up with the necessary resources.
     */
    fun prepare()

    /**
     * Releases the resources associated with the player.
     */
    fun release()

    /**
     * Sets the media item (e.g., a video) that the player should play based on a provided URL.
     *
     * @param url The URL pointing to the media resource.
     */
    fun setMediaItem(url: String)

    /**
     * Seeks to a specific playback position within a given media item (or window).
     *
     * @param window The index of the media item to which to seek.
     * @param position The position within the media item in milliseconds.
     */
    fun seekTo(window: Int, position: Long)

    /**
     * Registers a listener to be called when the video playback reaches its end.
     *
     * @param listener A lambda function that gets triggered when the playback ends.
     */
    fun addEndOfVideoListener(listener: () -> Unit)
}
