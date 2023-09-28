package com.maverkick.tasks.types.true_or_false

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

/**
 * A Parcelable data class to represent a True or False task.
 *
 * @property type Task type specific to True or False tasks, set to TaskType.TRUE_OR_FALSE.
 * @property statement The statement that needs to be evaluated as True or False.
 * @property answer The correct answer for the statement, represented as a String either "True" or "False".
 */
@Parcelize
data class TrueOrFalse(
    override val type: TaskType = TaskType.TRUE_OR_FALSE,
    val statement: String = "",
    val answer: String = ""
) : Task(type), Parcelable {
    override fun createFragment(): Fragment {
        return TrueOrFalseFragment.newInstance(this)
    }
}
