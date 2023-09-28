package com.maverkick.data.repositories

import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.TextLesson
import com.maverkick.data.models.TextLessonFirebase
import javax.inject.Inject

/**
 * Class responsible for the TextLesson objects interaction with
 * the Cloud Database using Dependency Injection
 * @param databaseService - database to which we're connecting
 **/
class TextLessonRepository @Inject constructor(
    private val databaseService: IDatabaseService
) {
    /** Get the text lessons for the course **/
    fun getTextCourseLessons(
        courseId: String,
        onSuccess: (List<TextLesson>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val lessonsRef = databaseService.db.collection("textCourses").document(courseId).collection("lessons")

        lessonsRef.orderBy("lessonOrder").get()
            .addOnSuccessListener { documents ->
                val textLessons = documents.mapNotNull { document ->
                    try {
                        val firebaseTextLesson = document.toObject(TextLessonFirebase::class.java)
                        // Convert firebaseTextLesson to TextLesson only if content is not null
                        firebaseTextLesson.toLesson(courseId, document.id)
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

    /** Get a specific text lesson by its id **/
    fun getTextLessonById(
        courseId: String,
        lessonId: String,
        onSuccess: (TextLesson?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val lessonRef =
            databaseService.db.collection("courses").document(courseId).collection("lessons").document(lessonId)

        lessonRef.get()
            .addOnSuccessListener { document ->
                val firebaseTextLesson = document.toObject(TextLessonFirebase::class.java)
                val textLesson = firebaseTextLesson?.toLesson(courseId,document.id)
                onSuccess(textLesson)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
