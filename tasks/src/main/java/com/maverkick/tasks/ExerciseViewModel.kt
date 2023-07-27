package com.maverkick.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.tasks.task.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel that is responsible for fetching the tasks
 * for the Exercise Dialog Fragment
 **/
@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    val checkAnswerEvent = MutableLiveData<Event<Pair<Boolean, String?>>>()

    // Cache of fetched tasks, mapped by course ID and lesson ID
    private val fetchedTasks = mutableMapOf<Pair<String, String>, List<Task>>()

    /**
     * This onCheckClicked() method is called when the check button is clicked.
     * It triggers the checkAnswerEvent which is observed in the ExerciseDialogFragment.
     **/
    fun onCheckClicked(isCorrect: Boolean, correctAnswer: String?) {
        checkAnswerEvent.value = Event(Pair(isCorrect, correctAnswer))
    }

    /** Load the tasks from the repository **/
    fun loadTasks(courseId: String, lessonId: String) {
        viewModelScope.launch {
            try {
                val tasks = fetchedTasks[Pair(courseId, lessonId)]
                    ?: taskRepository.fetchTasks(courseId, lessonId).also { fetchedTasks[Pair(courseId, lessonId)] = it }
                _tasks.value = tasks
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

