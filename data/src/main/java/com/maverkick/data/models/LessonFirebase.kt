package com.maverkick.data.models

open class LessonFirebase(
    val title: String = "",
    val duration: Int = 0,
    val lessonOrder: Int = 0
) {
    // Add a conversion function to be implemented in the child classes
    open fun toLesson(courseId: String, lessonId: String): Lesson {
        throw NotImplementedError("This method must be implemented in child classes.")
    }
}