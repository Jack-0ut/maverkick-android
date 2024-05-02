package com.maverkick.tasks

interface TaskActionsListener {

    /**
     * Performs a check of the user's answer to this task. The exact behavior of this
     * method will vary depending on the specifics of the task fragment that implements it.
     *
     * @param onResult a callback to be invoked with the result of the check.
     */
    fun checkAnswer(onResult: (Pair<Boolean, String?>) -> Unit)

    /**
     * Notifies when an option has been selected or deselected in the task.
     * This can be used to control UI elements like buttons based on whether an option is chosen.
     *
     * @param isSelected a Boolean indicating if an option has been selected.
     */
    fun onOptionSelected(isSelected: Boolean)
}


/**
 * Interface defining actions related to the selection of options within a task.
 *
 * Implementations of this interface will typically be used to listen for changes in the state
 * of an option (selected/deselected) and adapt the UI or the behavior of an activity or fragment accordingly.
 */
interface OptionSelectionListener {

    /**
     * Notifies when an option within a task has been selected or deselected.
     * This callback provides a way for the host (like an activity or another fragment) to
     * react to option changes and modify UI elements or behavior based on the current selection state.
     *
     * For instance, it can be used to enable or disable a "Submit" button based on whether an option is chosen.
     *
     * @param isSelected a Boolean indicating if an option has been selected.
     */
    fun onOptionSelected(isSelected: Boolean)
}
