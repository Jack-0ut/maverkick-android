package com.maverkick.tasks.quiz

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

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
