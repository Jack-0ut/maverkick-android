package com.maverkick.tasks.types.fill_in_gaps

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

/**
 * A Parcelable data class to represent a Fill-In-The-Gaps task in a quiz or similar context.
 *
 * @property type The type of the task, always set to TaskType.FILL_IN_GAPS for this class.
 * @property text The main question or statement that users are prompted to fill in the blanks for.
 * @property gaps A List of List of Strings, representing the options for each gap in the question. Each inner List contains the multiple-choice options for a single gap.
 */
@Parcelize
data class FillInGaps(
    override val type: TaskType = TaskType.FILL_IN_GAPS,
    val text: String = "",
    val gaps: List<Gap> = listOf()
) : Task(type), Parcelable {
    override fun createFragment(): Fragment {
        return FillInGapsFragment.newInstance(this)
    }
}

@Parcelize
data class Gap(
    val answer: String = "",
    val options: List<String> = listOf()
) : Parcelable
