package com.maverkick.tasks.open_answer

import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.maverkick.tasks.task.Task
import com.maverkick.tasks.task.TaskType
import kotlinx.parcelize.Parcelize

/** Class representing the open answer:
 * question, on which learner should give the open answer, like type or audio input
 * the data and then based on that we provide the assessment of the answer
 **/
@Parcelize
data class OpenAnswer(
    override val type: TaskType = TaskType.OPEN_ANSWER,
    val question: String = "",
    val description: String = "",
    val answer: String = "",
) : Task(type), Parcelable {
    override fun createFragment(): Fragment {
        return OpenAnswerFragment.newInstance(this)
    }
}
