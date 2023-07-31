package com.maverkick.tasks


import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.maverkick.data.IDatabaseService
import com.maverkick.tasks.fill_in_gaps.FillInBlanks
import com.maverkick.tasks.matching.Matching
import com.maverkick.tasks.open_answer.OpenAnswer
import com.maverkick.tasks.quiz.TextQuiz
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * Repository, which is responsible for working with tasks collection in the database:
 * fetching the tasks for a given lesson, maybe doing some updates or storing feedbacks
 **/
class TaskRepository @Inject constructor(private val databaseService: IDatabaseService) {
    /** Get the list of tasks for the given lesson **/
    suspend fun fetchTasks(courseId: String, lessonId: String): List<Task> {
        val tasksCollection = databaseService.db
            .collection("courses")
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

            for (document in documents) {
                val taskTypeString = document.getString("type")?.uppercase(Locale.ROOT)
                if (taskTypeString != null) {
                    try {
                        val task = when (TaskType.valueOf(taskTypeString)) {
                            TaskType.TEXT_QUIZ -> document.toObject(TextQuiz::class.java)
                            TaskType.FILL_IN_BLANKS -> document.toObject(FillInBlanks::class.java)
                            TaskType.OPEN_ANSWER -> document.toObject(OpenAnswer::class.java)
                            TaskType.MATCHING -> documentToMatchingTask(document)
                        }
                        if (task != null) {
                            taskList.add(task)
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e("TaskRepository", "Unknown task type: $taskTypeString")
                    }
                }
            }
            Log.d("TaskRepository", "Total tasks fetched: ${taskList.size}")
            taskList
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to fetch tasks", e)
            emptyList()
        }
    }
    /** Convert Firestore Document to the Matching Task Object **/
    private fun documentToMatchingTask(document: DocumentSnapshot): Matching? {
        val matchingData = document.toObject(Matching::class.java)
        if (matchingData != null) {
            val matchingTask = Matching(
                type = TaskType.MATCHING,
                question = matchingData.question,
                pairs = matchingData.pairs // Firestore automatically converts to MatchingPair objects
            )

            Log.d("Firestore", "Matching Task: $matchingTask")

            return matchingTask
        } else {
            return null
        }
    }
}

