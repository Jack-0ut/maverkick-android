package com.example.tasks

/**
 * This interface standardizes actions that can be performed on different types of tasks.
 *
 * By implementing this interface, task fragments indicate that they can perform these
 * actions, although the specific implementation details (i.e., what exactly happens
 * when the action is performed) are up to the individual fragments.
 *
 * This allows other components of the application, such as the ExerciseDialogFragment,
 * to interact with task fragments in a standardized way without needing to know the
 * specifics of what each task type does.
 */
interface TaskActionsListener {

    /**
     * Performs a check of the user's answer to this task. The exact behavior of this
     * method will vary depending on the specifics of the task fragment that implements it.
     *
     * For example, in a multiple-choice quiz task, this method might check which option
     * the user selected and compare it to the correct answer. In a fill-in-the-blank task,
     * it might compare the user's typed answer to the correct answer.
     */
    fun checkAnswer(): Pair<Boolean, String?>
}
