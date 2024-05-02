package com.example.shared_ui

/**
 * A utility class to measure the time duration of a lesson.
 *
 * Usage:
 * 1. Create an instance of the LessonTimer.
 * 2. Call `start()` method when you want to start the timer.
 * 3. Use the various utility methods to get the elapsed time.
 */
class LessonTimer {

    private var startTime: Long = 0L  // Timestamp when the timer was started

    /**
     * Starts or restarts the timer. Resets the internal timestamp.
     */
    fun start() {
        startTime = System.currentTimeMillis()
    }

    /**
     * Returns the duration since the timer was started in milliseconds.
     *
     * @return The duration in milliseconds.
     */
    fun getDuration(): Long {
        val currentTime = System.currentTimeMillis()
        return currentTime - startTime
    }

    /**
     * Returns the duration since the timer was started in seconds.
     *
     * @return The duration in seconds.
     */
    fun getDurationInSeconds(): Long {
        return getDuration() / 1000
    }
}
