package com.example.student.videolesson.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.student.databinding.FragmentExerciseBinding
import com.example.student.videolesson.exercise.tasks.TextQuizTaskFragment

/**
 * Fragment in which we're gonna be displaying the Tasks
 * Tasks could different: quiz, fill-in-gaps,true or false and
 * so this basically just the foundation for displaying them
 **/
class ExerciseDialogFragment : DialogFragment() {
    private var _binding: FragmentExerciseBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: TaskPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TaskPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Create a list of tasks
        val tasks = listOf(
            TextQuizTask("1", "What is the capital of the France?", listOf("London","Marseille","Paris", "Zurich"), 2),
            TextQuizTask("2", "Question 2", listOf("Option A", "Option B", "Option C", "Option D"), 3),
        )

        adapter.setTasks(tasks)

        // Initialize the ProgressBar
        binding.taskProgress.max = tasks.size
        binding.taskProgress.progress = 0

        // Button to skip the current task
        binding.skipButton.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
                binding.taskProgress.progress = nextItem
                updateButtons(nextItem)
            } else {
                // All tasks have been skipped, dismiss the dialog
                dismiss()
            }
        }

        // Button to move to the next task
        binding.nextButton.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
                binding.taskProgress.progress = nextItem
                updateButtons(nextItem)
            } else {
                // All tasks have been completed, dismiss the dialog
                dismiss()
            }
        }
    }

    private fun updateButtons(currentTaskIndex: Int) {
        val noMoreTasks = currentTaskIndex >= adapter.itemCount
        binding.nextButton.isEnabled = !noMoreTasks
        binding.skipButton.isEnabled = !noMoreTasks
    }

    /** Changing the dimension of the dialog to better fit user experience **/
    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
        binding.viewPager.isUserInputEnabled = false
        val width = (resources.displayMetrics.widthPixels * 0.98).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.9).toInt()
        dialog?.window?.setLayout(width, height)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ExerciseDialogFragment()
    }
}


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


