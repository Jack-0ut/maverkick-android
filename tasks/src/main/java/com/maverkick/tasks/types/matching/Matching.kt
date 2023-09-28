package com.maverkick.tasks.types.matching

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

/**
 * A data class representing a matching exercise task.
 *
 * This type of task presents the user with a list of pairs that need to be matched together.
 * Each pair consists of two strings.
 *
 * @property type An enumeration of the type of task. For a matching exercise, this will always be `TaskType.MATCHING`.
 * @property question The question text that will be displayed to the user. This is often used to provide instructions.
 * @property pairs A list of pairs to be matched in this exercise. Each pair consists of two strings: a term and its definition.
 */
@Parcelize
data class Matching(
    override val type: TaskType = TaskType.MATCHING,
    val question: String = "",
    val pairs: List<MatchingPair> = emptyList(),
) : Task(type), Parcelable {
    override fun createFragment(): Fragment {
        return MatchingFragment.newInstance(this)
    }
}

@Parcelize
data class MatchingPair(val key: String = "", val value: String = "") : Parcelable
