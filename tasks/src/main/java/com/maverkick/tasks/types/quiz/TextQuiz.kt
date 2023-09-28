package com.maverkick.tasks.types.quiz

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

/**
 * Represents a text quiz task.
 *
 * @property type The type of the task, specific to a text quiz. Default is [TaskType.TEXT_QUIZ].
 * @property question The question text that will be displayed to the user.
 * @property options A list of strings representing the multiple-choice options for the quiz.
 * @property answer The correct answer to the quiz. Must be one of the values in [options].
 */
@Parcelize
data class TextQuiz(
    override val type: TaskType = TaskType.TEXT_QUIZ,
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
) : Task(type), Parcelable {
    override fun createFragment(): Fragment {
        return TextQuizTaskFragment.newInstance(this)
    }
}
