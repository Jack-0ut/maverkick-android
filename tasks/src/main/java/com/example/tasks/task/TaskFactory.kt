package com.example.tasks.task

import androidx.fragment.app.Fragment
/**
 * The TaskFactory interface provides a mechanism for creating specific Task Fragments.
 *
 * Classes that represent different types of tasks should implement this interface to
 * provide their own logic for creating the corresponding fragment. This allows each
 * task type to encapsulate the logic of creating its associated fragment, promoting
 * better separation of concerns and making the code easier to maintain and extend.
 *
 * This follows the Factory Method design pattern, where an interface is used to define
 * a method for creating an object, but the actual implementation of this method is
 * deferred to implementing classes.
 */
interface TaskFactory {
    /**
     * Creates and returns a new Fragment instance associated with the current task type.
     *
     * The specific type of the Fragment (e.g., TextQuizFragment, FillInBlanksFragment, etc.)
     * and the data it contains will depend on the specific implementation in each task class.
     *
     * @return a new Fragment instance.
     */
    fun createFragment(): Fragment
}
