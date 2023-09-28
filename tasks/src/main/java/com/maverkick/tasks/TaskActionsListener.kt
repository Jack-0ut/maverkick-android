package com.maverkick.tasks

interface TaskActionsListener {
    /**
     * Performs a check of the user's answer to this task. The exact behavior of this
     * method will vary depending on the specifics of the task fragment that implements it.
     *
     * For example, in a multiple-choice quiz task, this method might check which option
     * the user selected and compare it to the correct answer. In a fill-in-the-blank task,
     * it might compare the user's typed answer to the correct answer.
     *
     * @param onResult a callback to be invoked with the result of the check.
     */
    fun checkAnswer(onResult: (Pair<Boolean, String?>) -> Unit)
}
