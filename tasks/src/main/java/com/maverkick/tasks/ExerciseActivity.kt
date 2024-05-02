package com.maverkick.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.maverkick.data.models.CourseType
import com.maverkick.tasks.databinding.ActivityExerciseBinding
import com.maverkick.tasks.quiz_summary.PostLessonSummaryActivity
import com.maverkick.tasks.task.TaskPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExerciseActivity : AppCompatActivity(), OptionSelectionListener {
    private lateinit var binding: ActivityExerciseBinding
    private val viewModel: ExerciseViewModel by viewModels()
    private lateinit var adapter: TaskPagerAdapter

    private var correctAnswersCount = 0
    private val finishedItems = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViews()
        observeViewModel()
        handleIntents()
    }

    private fun initializeViews() {
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)

        // disable check button, because initially it's not clicked on
        binding.checkButton.isEnabled = false

        adapter = TaskPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgress(position)

                if(finishedItems.contains(position)) {
                    binding.skipButton.visibility = View.GONE
                } else {
                    binding.skipButton.visibility = View.VISIBLE
                }
            }
        })

        binding.skipButton.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
                updateProgress(nextItem)
                setDefaultState()
            } else {
                navigateToQuizSummary()
            }
        }

        binding.checkButton.setOnClickListener {
            if (!binding.checkButton.isEnabled) return@setOnClickListener

            when (binding.checkButton.text) {
                getString(R.string.check_button_text) -> handleCheckButtonState()
                getString(R.string.next_button_text) -> handleNextButtonState()
            }
        }
    }

    private fun navigateToQuizSummary() {
        val courseId = intent.getStringExtra("courseId") ?: return
        val lessonId = intent.getStringExtra("lessonId") ?: return

        val totalQuestions = adapter.itemCount

        viewModel.finishLesson(courseId, lessonId)

        val redirectIntent = Intent(this, PostLessonSummaryActivity::class.java).apply {
            putExtra("CORRECT_ANSWERS", correctAnswersCount)
            putExtra("TOTAL_QUESTIONS", totalQuestions)
        }
        startActivity(redirectIntent)
        finish()
    }

    override fun onOptionSelected(isSelected: Boolean) {
        binding.checkButton.isEnabled = isSelected
    }

    /** Init the tasks when the activity is loaded first time **/
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
                if (result.first) {
                    correctAnswersCount++
                }
            }
        }
    }

    private fun handleCheckButtonState() {
        checkAnswerForCurrentFragment()
        updateUIAfterCheckingAnswer()
    }

    private fun handleNextButtonState() {
        navigateToNextQuestionOrSummary()
    }

    private fun checkAnswerForCurrentFragment() {
        val currentFragment = adapter.getFragment(binding.viewPager.currentItem)
        currentFragment?.let { fragment ->
            if (fragment is TaskActionsListener) {
                fragment.checkAnswer { result ->
                    viewModel.onCheckClicked(result.first, result.second)
                }
            }
        }
    }

    private fun updateUIAfterCheckingAnswer() {
        binding.checkButton.text = getString(R.string.next_button_text)
        binding.skipButton.visibility = View.GONE
        finishedItems.add(binding.viewPager.currentItem)
    }

    private fun navigateToNextQuestionOrSummary() {
        val nextItem = binding.viewPager.currentItem + 1
        if (nextItem < adapter.itemCount) {
            binding.viewPager.currentItem = nextItem
            updateProgress(nextItem)
        } else {
            navigateToQuizSummary()
        }
    }

    private fun handleIntents() {
        val courseId = intent.getStringExtra("courseId") ?: return
        val lessonId = intent.getStringExtra("lessonId") ?: return
        val courseType = CourseType.valueOf(intent.getStringExtra("courseType").toString())
        viewModel.loadTasks(courseId, lessonId, courseType)
    }

    private fun updateProgress(position: Int) {
        binding.taskProgress.progress = position
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
