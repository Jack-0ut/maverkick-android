package com.maverkick.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.event_bus.EventBus
import com.maverkick.data.event_bus.LessonCompletedEvent
import com.maverkick.data.models.CourseType
import com.maverkick.tasks.data.TaskRepository
import com.maverkick.tasks.task.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel responsible for managing the exercises within the application.
 * @property taskRepository The repository providing tasks for exercises.
 * @property _tasks A private MutableStateFlow representing the current tasks.
 * @property tasks A StateFlow view of the tasks for observation.
 * @property _checkAnswerEvent A private MutableLiveData for managing check answer events.
 * @property checkAnswerEvent A LiveData view of check answer events for observation.
 * @property fetchedTasks A cache of fetched tasks, mapped by course ID and lesson ID.
 */
@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _checkAnswerEvent = MutableLiveData<Event<Pair<Boolean, String?>>>()
    val checkAnswerEvent: LiveData<Event<Pair<Boolean, String?>>> = _checkAnswerEvent

    // Cache of fetched tasks, mapped by course ID and lesson ID
    private val fetchedTasks = mutableMapOf<Pair<String, String>, List<Task>>()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading


    /**
     * Handles the click event for checking an answer.
     * @param isCorrect A flag indicating whether the answer is correct.
     * @param correctAnswer The correct answer, if provided.
     */
    fun onCheckClicked(isCorrect: Boolean, correctAnswer: String?) {
        _checkAnswerEvent.value = Event(Pair(isCorrect, correctAnswer))
    }

    /**
     * Loads the tasks for the given course and lesson IDs.
     * @param courseId The ID of the course.
     * @param lessonId The ID of the lesson.
     * @param courseType The type of the course.
     */
    fun loadTasks(courseId: String, lessonId: String, courseType: CourseType) {
        _isLoading.value = true
        viewModelScope.launch {
            val tasksKey = Pair(courseId, lessonId)
            val tasks = fetchedTasks[tasksKey]
                ?: taskRepository.fetchTasks(courseId, lessonId, courseType).also { fetchedTasks[tasksKey] = it }
            _tasks.value = tasks
            _isLoading.value = false
        }
    }

    /**
     * Marks a lesson as completed and emits an event for updating the database.
     * @param courseId The ID of the course.
     * @param lessonId The ID of the lesson.
     */
    fun finishLesson(courseId: String, lessonId: String) {
        viewModelScope.launch {
            EventBus.lessonCompletedEvent.emit(LessonCompletedEvent(courseId, lessonId))
        }
    }
}

/**
 * A class to handle one-time events.
 * @param T The type of the content.
 * @property content The content to be handled.
 */
class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    /**
     * Returns the content if it hasn't been handled, or null otherwise.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}