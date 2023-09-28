package com.maverkick.tasks.data


import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.maverkick.data.IDatabaseService
import com.maverkick.data.models.CourseType
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import com.maverkick.tasks.types.fill_in_gaps.FillInGaps
import com.maverkick.tasks.types.fill_in_gaps.Gap
import com.maverkick.tasks.types.matching.Matching
import com.maverkick.tasks.types.matching.MatchingPair
import com.maverkick.tasks.types.quiz.TextQuiz
import com.maverkick.tasks.types.true_or_false.TrueOrFalse
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Repository, which is responsible for working with tasks collection in the database:
 * fetching the tasks for a given lesson, maybe doing some updates or storing feedbacks
 **/
class TaskRepository @Inject constructor(private val databaseService: IDatabaseService) {
    /** Get the list of tasks for the given lesson **/
    suspend fun fetchTasks(courseId: String, lessonId: String, courseType: CourseType): List<Task> {
        val collectionName = when (courseType) {
            CourseType.TEXT -> "textCourses"
            CourseType.VIDEO -> "courses"
        }
        val tasksCollection = databaseService.db
            .collection(collectionName)
            .document(courseId)
            .collection("lessons")
            .document(lessonId)
            .collection("tasks")

        val taskList = mutableListOf<Task>()
        return try {
            val documents = tasksCollection
                .orderBy("type")
                .get()
                .await()

            Log.d("TaskRepository", "Fetched ${documents.size()} tasks documents for courseId: $courseId, lessonId: $lessonId")

            for (document in documents) {
                try {
                    Log.d("TaskRepository", "Document before conversion: ${document.data}")

                    val taskTypeString = document.getString("type")?.uppercase(Locale.ROOT)
                    if (taskTypeString != null) {
                        val task = when (TaskType.valueOf(taskTypeString)) {
                            TaskType.TEXT_QUIZ -> document.toObject(TextQuiz::class.java)
                            TaskType.TRUE_OR_FALSE -> document.toObject(TrueOrFalse::class.java)
                            TaskType.FILL_IN_GAPS -> documentToFillInGapsTask(document)
                            TaskType.MATCHING -> documentToMatchingTask(document)
                        }
                        if (task != null) {
                            Log.d("TaskRepository",task.toString())
                            taskList.add(task)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TaskRepository", "Failed to convert task: ${document.id}", e)
                }
            }
            taskList
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to fetch tasks", e)
            emptyList()
        }
    }

    /** Convert Firestore Document to the Matching Task Object **/
    fun documentToMatchingTask(document: DocumentSnapshot): Matching? {
        val question = document.getString("question") ?: return null
        val type = TaskType.valueOf(document.getString("type") ?: return null)
        val pairsList = document.get("pairs") as? List<Map<String, String>> ?: return null
        val pairs = pairsList.mapNotNull { map ->
            val key = map["key"] ?: return@mapNotNull null
            val value = map["value"] ?: return@mapNotNull null
            MatchingPair(key, value)
        }
        return Matching(type, question, pairs)
    }


    private fun documentToFillInGapsTask(document: DocumentSnapshot): FillInGaps? {
        val text = document.getString("text") ?: return null
        val gapsList = document.get("gaps") as? List<Map<String, Any>> ?: return null

        val gaps = gapsList.mapNotNull { gapMap ->
            val answer = gapMap["answer"] as? String ?: return@mapNotNull null
            val options = gapMap["options"] as? List<String> ?: return@mapNotNull null
            Gap(answer, options)
        }

        return FillInGaps(text = text, gaps = gaps)
    }

}
