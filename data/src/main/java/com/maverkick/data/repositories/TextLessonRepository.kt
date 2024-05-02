package com.maverkick.data.repositories

import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.TextLesson
import com.maverkick.data.models.TextLessonFirebase
import javax.inject.Inject

/**
 * Class responsible for the GeneratedTextLesson objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class TextLessonRepository @Inject constructor(
    private val databaseService: IDatabaseService
) {

    fun getTextLessons(
        courseId: String,
        courseType: CourseType,
        onSuccess: (List<TextLesson>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val collectionName = when(courseType) {
            CourseType.TEXT_PERSONALIZED -> "generatedCourses"
            CourseType.TEXT -> "courses"
            else -> throw IllegalArgumentException("Invalid course type: $courseType")
        }

        val lessonsRef = databaseService.db.collection(collectionName).document(courseId).collection("lessons")

        lessonsRef.orderBy("lessonOrder").get()
            .addOnSuccessListener { documents ->
                val textLessons = documents.mapNotNull { document ->
                    try {
                        val firebaseTextLesson = document.toObject(TextLessonFirebase::class.java)
                        val textLesson = firebaseTextLesson.toLesson(courseId, document.id)
                        textLesson
                    } catch (e: Exception) {
                        null // Ignore documents that can't be converted to TextLessonFirebase
                    }
                }
                onSuccess(textLessons)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
