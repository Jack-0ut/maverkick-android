package com.example.student.exercise.tasks

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.student.exercise.Task
import com.example.student.exercise.TaskType
import com.example.student.exercise.TextQuizTask

/**
 * Adapter class which is responsible for displaying the list of different
 * tasks to the user, also choosing which tasks are gonna be displayed and
 * how many of them we should display
 **/
class TaskPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var tasks: List<Task> = emptyList()

    /** Method that sets the list of tasks for this Exercise **/
    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun getItemCount() = tasks.size

    /** This is used for creation of different tasks for the same lesson**/
    override fun createFragment(position: Int): Fragment {
        val task = tasks[position]
        return when (task.type) {
            TaskType.QUIZ -> TextQuizTaskFragment.newInstance(task as TextQuizTask)
            // Add more types here as needed
            else -> throw IllegalArgumentException("Unsupported task type: ${task.type}")
        }
    }

}
