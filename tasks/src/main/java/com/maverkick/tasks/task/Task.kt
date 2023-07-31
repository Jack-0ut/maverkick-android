package com.maverkick.tasks.task

/**
 * Base class for tens or even hundreds of possible tasks that could be.
 * This is used as a foundation and for the ExerciseDialogFragment initialization
 **/
abstract class Task(
    open val type: TaskType
) : TaskFactory


/**
 * Class that store all of the possible Task Classes
 **/
enum class TaskType {
    TEXT_QUIZ,
    FILL_IN_BLANKS,
    OPEN_ANSWER,
    MATCHING
}
