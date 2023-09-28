package com.maverkick.data.models

import java.util.*

/**
 * Class storage for the TextCourse objects
 * @param courseId - unique id for the course
 * @param courseName- name that defines the course, shouldn't be uniques
 * @param studentId - id of the creator of that course (since text courses are created by students)
 * @param numberLessons - total number of lessons
 * @param language - the language of the course
 * @param creationDate - date of the course creation
 **/

data class TextCourse(
    override val courseId: String,
    override val courseName: String,
    val studentId: String,
    override val numberLessons: Int,
    override val language: String,
    override val creationDate: Date
) : Course(courseId, courseName, language, numberLessons, creationDate, CourseType.TEXT) {

    fun toFirebaseTextCourse(): FirebaseTextCourse {
        return FirebaseTextCourse(
            courseName = this.courseName,
            studentId = this.studentId,
            lessonCount = this.numberLessons,
            language = this.language,
            creationDate = this.creationDate,
        )
    }
}

/**
 * The same TextCourse class, but without courseId, specifically for adding to Firebase
 */
data class FirebaseTextCourse @JvmOverloads constructor(
    val courseName: String = "",
    val studentId: String = "",
    val lessonCount: Int = 0,
    val language: String = "",
    val creationDate: Date = Date(),
) {
    fun toCourse(courseId: String): TextCourse {
        return TextCourse(
            courseId,
            courseName,
            studentId,
            lessonCount,
            language,
            creationDate,
        )
    }
}
