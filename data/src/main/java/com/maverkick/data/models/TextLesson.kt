package com.maverkick.data.models

/**
 * Class storage for the TextLesson objects
 * @param lessonId - id of the lesson
 * @param courseId - id of the course
 * @param title - title of the text-lesson
 * @param duration - approximate time average user could read it in
 * @param content - the main text content of the lesson
 * @param lessonOrder - the order of the lesson in the course (1,2,3,4...)
 **/
data class TextLesson(
    override val lessonId: String = "",
    override val courseId: String = "",
    override val title: String = "",
    override val duration: Int = 0,
    override val lessonOrder: Int = 0,
    val content: String = ""
) : Lesson(lessonId, courseId, title, duration, lessonOrder)

/**
 * The same TextLesson class, but without lessonId and courseId, specifically for interacting with Firebase
 **/
class TextLessonFirebase(
    title: String = "",
    val content: String = "",
    duration: Int = 0,
    lessonOrder: Int = 0
) : LessonFirebase(title, duration, lessonOrder) {
    override fun toLesson(courseId: String, lessonId: String): TextLesson {
        return TextLesson(
            lessonId,
            courseId,
            title,
            duration,
            lessonOrder,
            content
        )
    }
}