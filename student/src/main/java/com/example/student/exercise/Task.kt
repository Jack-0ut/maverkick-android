package com.example.student.exercise

import android.os.Parcel
import android.os.Parcelable

/**
 * Base class for tens or even hundreds of possible tasks that could be.
 * This is used as a foundation and for the ExerciseDialogFragment initialization
 * @param id - corresponds to the id of the current task
 * @param type - correspond to the type of the Task (Quiz,CompleteSentence,FindError)
 **/
abstract class Task(open val id: String, open val type: TaskType)

/**
 * Specific Task class for the Text Quiz (The Question and 4 Options to Answer),
 * with only 1 correct
 **/
data class TextQuizTask(
    override val id: String,
    val question: String,
    val options: List<String>,
    val correctOption: Int
) : Task(id, TaskType.QUIZ), Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(question)
        dest.writeStringList(options)
        dest.writeInt(correctOption)
    }

    companion object CREATOR : Parcelable.Creator<TextQuizTask> {
        override fun createFromParcel(parcel: Parcel): TextQuizTask {
            val id = parcel.readString()
            val question = parcel.readString()
            val options = mutableListOf<String>()
            parcel.readStringList(options)
            val correctOption = parcel.readInt()
            return TextQuizTask(id!!, question!!, options, correctOption)
        }

        override fun newArray(size: Int): Array<TextQuizTask?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Class that store all of the possible Task Classes
 **/
enum class TaskType {
    QUIZ,
}
