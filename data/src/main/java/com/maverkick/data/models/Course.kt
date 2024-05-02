package com.maverkick.data.models

import java.util.Date

enum class CourseType {
    TEXT_PERSONALIZED, VIDEO, TEXT
}

/**
 * Abstract class representing a course.
 *
 * @property courseId       Unique identifier for the course.
 * @property courseName     Name of the course.
 * @property language       Language of the course content.
 * @property numberLessons  Total number of lessons in the course.
 * @property creationDate   Date of course creation.
 * @property type           Type of the course (text or video).
 * @property authorId       Unique identifier for the author of the course.
 */
abstract class Course(
    open val courseId: String,
    open val courseName: String,
    open val language: String,
    open val numberLessons: Int,
    open val creationDate: Date,
    open val type: CourseType,
    open val authorId: String 
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Course

        if (courseId != other.courseId) return false
        if (courseName != other.courseName) return false
        if (language != other.language) return false
        if (numberLessons != other.numberLessons) return false
        if (creationDate != other.creationDate) return false
        if (type != other.type) return false
        if (authorId != other.authorId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId.hashCode()
        result = 31 * result + courseName.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + numberLessons
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + authorId.hashCode()
        return result
    }
}
