package com.maverkick.tasks.task

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter class which is responsible for displaying the list of different
 * tasks to the user, also choosing which tasks are gonna be displayed and
 * how many of them we should display
 **/
class TaskPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private var tasks: List<Task> = emptyList()

    // SparseArray to keep track of the fragments in the ViewPager
    private val fragmentMap = SparseArray<Fragment>()

    /** Method that sets the list of tasks for this Exercise **/
    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun getItemCount() = tasks.size

    /** This is used for creation of different tasks for the same lesson**/
    override fun createFragment(position: Int): Fragment {
        // The creation logic is now delegated to the task itself
        val fragment = tasks[position].createFragment()

        // Save the fragment instance to the map
        fragmentMap.put(position, fragment)

        return fragment
    }

    /** Method to get a reference to the fragment at the given position **/
    fun getFragment(position: Int): Fragment? {
        return fragmentMap.get(position)
    }
}
