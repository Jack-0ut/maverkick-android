package com.maverkick.data.models

/**
 * ILesson is a common abstract class for different types of lessons within the application.
 * This abstract class helps unify the handling of various lesson types, such as video and text lessons,
 * making it easier to create shared functionality around these types.
 *
 * @property lessonId - A unique identifier for the lesson.
 * @property courseId - The identifier of the course to which the lesson belongs.
 * @property title - The title of the lesson.
 * @property duration - The duration of the lesson in seconds.
 *                  For video lessons, it represents the length of the video,
 *                  while for text lessons, it may represent an estimated reading time.
 * @property lessonOrder - The order of the lesson within the course (e.g. 1, 2, 3, 4,...).
 */
abstract class Lesson(
    open val lessonId: String,
    open val courseId: String,
    open val title: String,
    open val duration: Int,
    open val lessonOrder: Int
) {
    fun isContentTheSame(other: Lesson): Boolean {
        return lessonId == other.lessonId &&
                courseId == other.courseId &&
                title == other.title &&
                duration == other.duration &&
                lessonOrder == other.lessonOrder
    }
}