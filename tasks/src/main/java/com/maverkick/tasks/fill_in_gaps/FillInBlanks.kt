package com.maverkick.tasks.fill_in_gaps

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

// In FillInBlanks.kt
@Parcelize
data class FillInBlanks(
    override val type: TaskType = TaskType.FILL_IN_BLANKS,
    val question: String = "",
    val answer: String = "",
) : Task(type), Parcelable {
    override fun createFragment(): Fragment {
        return FillInBlanksFragment.newInstance(this)
    }
}
