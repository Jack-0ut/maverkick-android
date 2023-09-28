package com.maverkick.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.CourseType
import com.maverkick.tasks.databinding.FragmentExerciseBinding
import com.maverkick.tasks.task.TaskPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExerciseActivity : AppCompatActivity() {
    private lateinit var binding: FragmentExerciseBinding
    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var adapter: TaskPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViews()
        initializeSnackbar()
        observeViewModel()
        handleIntents()
    }

    private fun initializeViews() {
        binding = FragmentExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_accent_light)
        adapter = TaskPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgress(position)
            }
        })

        binding.skipButton.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
                updateProgress(nextItem) // Update progress using the helper function
                setDefaultState() // Revert to the default state
            } else {
                finishExercises()
            }
        }

        binding.checkButton.setOnClickListener {
            if (binding.checkButton.text == getString(R.string.check_button_text)) {
                val currentFragment = adapter.getFragment(binding.viewPager.currentItem)
                currentFragment?.let { fragment ->
                    if (fragment is TaskActionsListener) {
                        fragment.checkAnswer { result ->
                            viewModel.onCheckClicked(result.first, result.second)
                        }
                    }
                }
                binding.checkButton.text = getString(R.string.next_button_text)
                binding.skipButton.visibility = View.GONE // Hide the Skip button
            } else {
                val nextItem = binding.viewPager.currentItem + 1
                if (nextItem < adapter.itemCount) {
                    binding.viewPager.currentItem = nextItem
                    updateProgress(nextItem) // Update progress using the helper function
                } else {
                    finishExercises()
                }
            }
        }
    }

    private fun initializeSnackbar(): Snackbar {
        return Snackbar.make(binding.coordinatorLayout, "", Snackbar.LENGTH_SHORT).apply {
            val snackbarView = view
            val snackbarText = snackbarView.findViewById<TextView>(R.id.snackbar_text)
            snackbarText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            snackbarText.maxLines = 5
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    binding.taskProgress.max = tasks.size
                    if (tasks.isEmpty() && !viewModel.isLoading.value) {
                        finishExercises()
                    } else {
                        adapter.setTasks(tasks)
                        binding.taskProgress.progress = 0
                    }
                }
            }
        }

        viewModel.checkAnswerEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                showFeedback(result)
            }
        }
    }

    private fun handleIntents() {
        val courseId = intent.getStringExtra("courseId") ?: return
        val lessonId = intent.getStringExtra("lessonId") ?: return
        val courseType = CourseType.valueOf(intent.getStringExtra("courseType").toString())

        viewModel.loadTasks(courseId, lessonId, courseType)
    }

    private fun showFeedback(result: Pair<Boolean, String?>) {
        val feedbackText = result.second ?: if (result.first) {
            "Great job! You're cool!'"
        } else {
            "Keep going, you'll get there!)"
        }

        val snackbarColor = ContextCompat.getColor(this, if (result.first) com.maverkick.common.R.color.green else com.maverkick.common.R.color.red)

        Snackbar.make(binding.coordinatorLayout, feedbackText, Snackbar.LENGTH_SHORT).apply {
            view.setBackgroundColor(snackbarColor)
            val snackbarText = view.findViewById<TextView>(R.id.snackbar_text)
            snackbarText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            snackbarText.maxLines = 5
            show()
        }
    }

    private fun updateProgress(position: Int) {
        binding.taskProgress.progress = position + 1
        setDefaultState()
    }

    private fun setDefaultState() {
        binding.checkButton.text = getString(R.string.check_button_text)
        binding.skipButton.visibility = View.VISIBLE
    }

    private fun finishExercises() {
        val courseId = intent.getStringExtra("courseId") ?: return
        val lessonId = intent.getStringExtra("lessonId") ?: return

        viewModel.finishLesson(courseId, lessonId)

        val redirectIntent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://student/studentHomeFragment"))
        startActivity(redirectIntent)
        finish()
    }

    companion object {
        fun newIntent(context: Context, courseId: String, lessonId: String, courseType: CourseType): Intent {
            return Intent(context, ExerciseActivity::class.java).apply {
                putExtra("courseId", courseId)
                putExtra("lessonId", lessonId)
                putExtra("courseType", courseType.name)
            }
        }
    }
}
