package com.maverkick.data.models

import java.util.*

/**
 * Class storage for the Lesson objects
 * @param lessonId - id of the lesson
 * @param courseId - id of the course
 * @param title - title of the video-lesson
 * @param duration - length of the video-lesson (in seconds) <= 300
 * @param videoUrl - path to the storage where video is stored
 * @param transcription - text transcription of what is said in the video
 * @param lessonOrder - the order of the lesson in the course (1,2,3,4...)
 * @param creationDate - the date of the video lesson creation
 **/
data class VideoLesson(
    override val lessonId: String = "",
    override val courseId: String = "",
    override val title: String = "",
    override val duration: Int = 0,
    override val lessonOrder: Int = 0,
    val videoUrl: String = "",
    val transcription: String = "",
    val creationDate: Date = Date()
) : Lesson(lessonId, courseId, title, duration, lessonOrder)
/**
 * The same Lesson class, but without lessonId and courseId, specifically for interacting with Firebase
 **/
class VideoLessonFirebase(
    title: String = "",
    duration: Int = 0,
    val videoUrl: String? = null,
    val transcription: String? = null,
    lessonOrder: Int = 0,
    private val creationDate: Date = Date()
) : LessonFirebase(title, duration, lessonOrder) {
    override fun toLesson(courseId: String, lessonId: String): VideoLesson {
        return VideoLesson(
            lessonId,
            courseId,
            title,
            duration,
            lessonOrder,
            videoUrl ?: "",
            transcription ?: "",
            creationDate
        )
    }
}
