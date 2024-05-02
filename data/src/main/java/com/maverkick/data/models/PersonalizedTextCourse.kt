package com.maverkick.data.models

import java.util.*

/**
 * Class storage for the PersonalizedTextCourse objects
 * @param courseId - unique id for the course
 * @param courseName - name that defines the course; shouldn't be unique
 * @param authorId - id of the creator of that course (since text courses are created by students)
 * @param numberLessons - total number of lessons
 * @param language - the language of the course
 * @param creationDate - date of the course creation
 **/
data class PersonalizedTextCourse(
    override val courseId: String,
    override val courseName: String,
    override val authorId: String, // Replaced studentId with authorId
    override val numberLessons: Int,
    override val language: String,
    override val creationDate: Date
) : Course(courseId, courseName, language, numberLessons, creationDate, CourseType.TEXT_PERSONALIZED, authorId)
/**
 * The same PersonalizedTextCourse class, but without courseId
 * to reflect the structure of the Firebase document
 */
data class FirebasePersonalizedTextCourse @JvmOverloads constructor(
    val courseName: String = "",
    val authorId: String = "",
    val lessonCount: Int = 0,
    val language: String = "",
    val creationDate: Date = Date(),
) {
    fun toCourse(courseId: String): PersonalizedTextCourse {
        return PersonalizedTextCourse(
            courseId,
            courseName,
            authorId,
            lessonCount,
            language,
            creationDate,
        )
    }
}

