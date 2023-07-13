package com.example.tasks

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tasks.databinding.FragmentExerciseBinding
import com.example.tasks.task.TaskPagerAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment in which we're gonna be displaying the Tasks
 * Tasks could different: quiz, fill-in-gaps,true or false and so on
 * So this is basically the foundation for displaying them
 **/
@AndroidEntryPoint
class ExerciseDialogFragment : DialogFragment() {
    private var _binding: FragmentExerciseBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TaskPagerAdapter
    val viewModel: ExerciseViewModel by activityViewModels()

    // Callback on when student solves all the tasks
    interface ExerciseDialogListener {
        fun onExercisesCompleted()
    }

    // Define a variable to hold the listener
    private var listener: ExerciseDialogListener? = null

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

        val courseId = arguments?.getString("courseId")
        val lessonId = arguments?.getString("lessonId")

        if (courseId != null && lessonId != null) {
            viewModel.loadTasks(courseId, lessonId)
        }

        adapter = TaskPagerAdapter(this)
        binding.viewPager.adapter = adapter

        val snackbar = Snackbar.make(binding.coordinatorLayout, "", Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackbarText.textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackbarText.maxLines = 5

        // Collect tasks from ViewModel and set to adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    adapter.setTasks(tasks)
                    // Initialize the ProgressBar
                    binding.taskProgress.max = tasks.size
                    binding.taskProgress.progress = 0
                }
            }
        }

        // Button to skip the current task
        binding.skipButton.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
                binding.taskProgress.progress = nextItem
                updateButtons(nextItem)
            } else {
                // All tasks have been skipped, dismiss the dialog
                listener?.onExercisesCompleted()
                dismiss()
            }
        }

        viewModel.checkAnswerEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                // The check answer event has been triggered, call the checkAnswer function
                val currentFragment = adapter.getFragment(binding.viewPager.currentItem)
                currentFragment?.let { fragment ->
                    if (fragment is TaskActionsListener) {
                        fragment.checkAnswer()
                    }
                }

                // Show the snackbar with feedback
                // Change the color and message of the snackbar based on the result
                if (result.first) {
                    snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), com.example.common.R.color.green))
                    snackbar.setText("Great job!")
                } else {
                    snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), com.example.common.R.color.red))
                    snackbar.setText("The correct answer is: ${result.second}")
                }

                snackbar.show()
            }
        }

        binding.checkButton.setOnClickListener {
            if (binding.checkButton.text == "Check") {
                // Check the answer here
                val currentFragment = adapter.getFragment(binding.viewPager.currentItem)
                currentFragment?.let { fragment ->
                    if (fragment is TaskActionsListener) {
                        val result = fragment.checkAnswer()
                        viewModel.onCheckClicked(result.first, result.second)
                    }
                }

                // Change the button text to "Continue" and hide the Skip button
                binding.checkButton.text = "Continue"
                binding.skipButton.visibility = View.GONE
            } else {
                // Move to the next task
                val nextItem = binding.viewPager.currentItem + 1
                if (nextItem < adapter.itemCount) {
                    binding.viewPager.currentItem = nextItem
                    binding.taskProgress.progress = nextItem
                    updateButtons(nextItem)
                } else {
                    // All tasks have been completed, call the callback and close the dialog
                    listener?.onExercisesCompleted()
                    dismiss()
                }

                // Reset the button text to "Check" and make the Skip button visible
                binding.checkButton.text = "Check"
                binding.skipButton.visibility = View.VISIBLE
            }
        }

    }

    /** When we have no more tasks, just hide all the buttons **/
    private fun updateButtons(currentTaskIndex: Int) {
        val noMoreTasks = currentTaskIndex >= adapter.itemCount
        binding.skipButton.isEnabled = !noMoreTasks
        binding.checkButton.isEnabled = !noMoreTasks
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Check if the dialog exists and is currently being shown
        if (dialog != null && dialog?.isShowing == true) {
            val width = (resources.displayMetrics.widthPixels * 0.98).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.9).toInt()
            dialog?.window?.setLayout(width, height)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExerciseDialogListener) {
            listener = context
        } else {
            throw ClassCastException("$context must implement ExerciseDialogListener")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(courseId: String, lessonId: String): ExerciseDialogFragment {
            val fragment = ExerciseDialogFragment()

            // Arguments to pass to the fragment
            val args = Bundle().apply {
                putString("courseId", courseId)
                putString("lessonId", lessonId)
            }

            fragment.arguments = args
            return fragment
        }
    }
}


