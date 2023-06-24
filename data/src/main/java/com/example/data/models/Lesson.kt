package com.example.data.models

import java.util.*

/**
 * Class storage for the Lesson objects
 * @param lessonId - id of the lesson
 * @param title - title of the video-lesson
 * @param duration - length of the video-lesson (in seconds) <= 300
 * @param videoUrl - path to the storage where video is stored
 * @param transcription - text transcription of what is said in the video
 * @param lessonOrder - the order of the lesson in the course (1,2,3,4...)
 * @param creationDate - the date of the video lesson creation
 **/
data class Lesson(
    val lessonId: String,
    val title: String,
    val duration: Int,
    val videoUrl: String,
    val transcription: String,
    val lessonOrder: Int,
    val creationDate: Date
) {
    fun toFirebaseLesson(): LessonFirebase {
        return LessonFirebase(
            title = this.title,
            duration = this.duration,
            videoUrl = this.videoUrl,
            transcription = this.transcription,
            lessonOrder = this.lessonOrder,
            creationDate = this.creationDate
        )
    }
}

/**
 * The same Lesson class, but without lessonId, specifically for interacting with Firebase
 **/
data class LessonFirebase @JvmOverloads constructor(
    val title: String = "",
    val duration: Int = 0,
    val videoUrl: String? = null,
    val transcription: String? = null,
    val lessonOrder: Int = 0,
    val creationDate: Date = Date()
) {
    fun toLesson(lessonId: String): Lesson {
        return Lesson(
            lessonId,
            title,
            duration,
            videoUrl ?: "",  // if videoUrl is null, use empty string
            transcription ?: "",  // if transcription is null, use empty string
            lessonOrder,
            creationDate
        )
    }
}
