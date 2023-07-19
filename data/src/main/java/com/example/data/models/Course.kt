package com.example.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Class storage for the Course objects
 * @param courseId - unique id for the course
 * @param courseName- name that defines the course, shouldn't be uniques
 * @param teacherId - id of the creator of that course
 * @param language - the language of the course
 * @param poster - image/poster for the course to make it appealing and clear
 * @param tags - tags describing 5 things on which course is concentrating
 * @param creationDate - date of the course creation
 * @param published - is this course available for the students
 **/

data class Course(
    val courseId: String,
    val courseName: String,
    val teacherId: String,
    val language: String,
    var poster: String?,
    var lessonCount: Int,
    val tags: List<String>,
    val creationDate: Date,
    val published: Boolean
) {
    fun toFirebaseCourse(): FirebaseCourse {
        return FirebaseCourse(
            courseName = this.courseName,
            teacherId = this.teacherId,
            language = this.language,
            poster = this.poster ?: "",
            lessonCount = this.lessonCount,
            tags = this.tags,
            creationDate = this.creationDate,
            published = this.published
        )
    }
}

/**
 * The same Course class, but without courseId, specifically for adding to Firebase
 */
data class FirebaseCourse @JvmOverloads constructor(
    val courseName: String = "",
    val teacherId: String = "",
    val language: String = "",
    val poster: String? = null,
    val lessonCount: Int = 0,
    val tags: List<String> = listOf(),
    val creationDate: Date = Date(),
    val published: Boolean = false
) {
    fun toCourse(courseId: String): Course {
        return Course(
            courseId,
            courseName,
            teacherId,
            language,
            poster,
            lessonCount,
            tags,
            creationDate,
            published
        )
    }
}

/** Course class representation in the algolia index, used for the courses search*/
@Serializable
data class SearchCourseHit(
    @SerialName("objectID")
    val objectId: String, // this is the Algolia objectID, which is courseId
    val courseName: String,
    val language: String,
    val poster: String?,
    val tags: List<String>
    )
