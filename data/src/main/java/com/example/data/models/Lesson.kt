package com.example.data.models

/**
 * Class storage for the Lesson objects
 * @param lessonId - id of the lesson
 * @param title - title of the video-lesson
 * @param length - length of the video-lesson (in seconds) <= 300
 * @param videoUrl - path to the storage where video is
 * @param transcription - text transcription of what is said in the video
 * @param lessonOrder - the order of the lesson in the course (1,2,3,4...)
 **/


data class Lesson(
    val lessonId: String,
    val title: String,
    val length: Int,
    val videoUrl: String,
    val transcription: String,
    val lessonOrder: Int
    )