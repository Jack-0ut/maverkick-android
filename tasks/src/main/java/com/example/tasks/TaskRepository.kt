package com.example.tasks


import android.util.Log
import com.example.data.IDatabaseService
import com.example.tasks.fill_in_gaps.FillInBlanks
import com.example.tasks.quiz.TextQuiz
import com.example.tasks.task.Task
import com.example.tasks.task.TaskType
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
                        }
                        taskList.add(task)
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
}

