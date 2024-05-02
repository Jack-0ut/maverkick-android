package com.maverkick.data.models

import java.util.*

data class TextCourse(
    override val courseId: String,
    override val courseName: String,
    override val language: String,
    var poster: String?,
    override val numberLessons: Int,
    val tags: List<String>,
    override val creationDate: Date,
    override val authorId: String,
    val published: Boolean
) : Course(courseId, courseName, language, numberLessons, creationDate, CourseType.TEXT, authorId)


data class FirebaseTextCourse @JvmOverloads constructor(
    val courseName: String = "",
    val language: String = "",
    val poster: String? = null,
    val lessonCount: Int = 0,
    val tags: List<String> = listOf(),
    val creationDate: Date = Date(),
    val authorId: String = "",
    val published: Boolean = false // Added published field with default value as false
) {
    fun toCourse(courseId: String): TextCourse {
        return TextCourse(
            courseId,
            courseName,
            language,
            poster,
            lessonCount,
            tags,
            creationDate,
            authorId,
            published
        )
    }
}

