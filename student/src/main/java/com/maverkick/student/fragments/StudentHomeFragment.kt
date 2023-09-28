package com.maverkick.student.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.event_bus.EventBus
import com.maverkick.data.models.Lesson
import com.maverkick.data.models.TextLesson
import com.maverkick.data.models.VideoLesson
import com.maverkick.student.R
import com.maverkick.student.adapters.LessonAdapter
import com.maverkick.student.databinding.FragmentStudentHomeBinding
import com.maverkick.student.videolesson.VideoLessonActivity
import com.maverkick.student.viewmodels.HomeViewModel
import com.maverkick.text_lesson.ui.TextLessonActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The Fragment for the Home Menu Item (Student)
 * It responsible for displaying today's lessons
 **/
@AndroidEntryPoint
class StudentHomeFragment : Fragment() {
    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    // update the course gen tries in the UI after it's being generated
    private var courseGenerationCompletedEventJob: Job? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToLessonCompletedEvent()
        subscribeToCourseWithdrawnEvent()

        // Set the status bar color
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), com.maverkick.common.R.color.maverkick_topbar)

        // if student doesn't have courses yet,navigate to the Gallery to choose first course
        viewModel.navigateToCourseEnrollment.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                val action = StudentHomeFragmentDirections.actionStudentHomeFragmentToGalleryFragment()
                findNavController().navigate(action)
                viewModel.onCourseEnrollmentNavigationComplete()
            }
        }

        // if it's the last lesson, remove button
        viewModel.isLastLesson.observe(viewLifecycleOwner) { isLast ->
            if (isLast) {
                binding.startLearningButton.visibility = View.GONE
            } else {
                binding.startLearningButton.visibility = View.VISIBLE
            }
        }

        binding.startLearningButton.setOnClickListener {
            val currentLessonIndex = viewModel.currentLessonIndex.value ?: 0
            val currentLesson = viewModel.dailyLearningPlan.value?.lessons?.get(currentLessonIndex)

            currentLesson?.let { lesson ->
                context?.let { context ->
                    createLessonIntent(context, lesson)?.let { intent -> startActivity(intent) }
                }
            }
        }

        // Init RecyclerView and pass the list of lessons that Student should learn today
        val initialIndex = viewModel.currentLessonIndex.value ?: 0
        val lessonsAdapter = LessonAdapter(object : OnItemClickListener<Lesson> {
            override fun onItemClick(item: Lesson) { onLessonClick(item) }
        }, initialIndex)

        binding.lessonsRecyclerView.adapter = lessonsAdapter
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(context)

        viewModel.currentLessonIndex.observe(viewLifecycleOwner) { newCurrentLessonIndex ->
            lessonsAdapter.updateCurrentLessonIndex(newCurrentLessonIndex)
        }

        viewModel.dailyLearningPlan.observe(viewLifecycleOwner) { dailyLearningPlan ->
            lessonsAdapter.submitList(dailyLearningPlan.lessons)
        }

        viewModel.bricksCollected.observe(viewLifecycleOwner) { bricks ->
            binding.pointsNumber.text = bricks.toString()
        }

        viewModel.courseGenerationTries.observe(viewLifecycleOwner){ tries ->
            binding.courseGenNumber.text = tries.toString()
        }

        binding.courseGenNumber.setOnClickListener {
            showPopup(it, "The number of courses you could create in this version.")
        }

        binding.pointsNumber.setOnClickListener {
            showPopup(it, "The number lessons you completed.")
        }
    }

    /** Click on the particular lesson **/
    fun onLessonClick(lesson: Lesson) {
        context?.let { context ->
            createLessonIntent(context, lesson)?.let { intent -> startActivity(intent) }
        }
    }

    /** Based on the lesson type, start particular activity **/
    private fun createLessonIntent(context: Context, lesson: Lesson): Intent? {
        return when (lesson) {
            is VideoLesson -> {
                Intent(context, VideoLessonActivity::class.java).apply {
                    putExtra("lessonId", lesson.lessonId)
                    putExtra("videoUri", lesson.videoUrl)
                    putExtra("transcription", lesson.transcription)
                    putExtra("title", lesson.title)
                    putExtra("courseId", lesson.courseId)
                }
            }
            is TextLesson -> {
                Intent(context, TextLessonActivity::class.java).apply {
                    putExtra("lessonId", lesson.lessonId)
                    putExtra("content", lesson.content)
                    putExtra("title", lesson.title)
                    putExtra("courseId", lesson.courseId)
                }
            }
            else -> null
        }
    }

    /** Receive the event from the 'Task' when lesson is completed **/
    private fun subscribeToLessonCompletedEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                EventBus.lessonCompletedEvent.collect { event ->
                    viewModel.handleLessonCompleted(event.lessonId, event.courseId)
                    EventBus.lessonCompletedEvent.resetReplayCache()

                }
            }
        }
    }

    /** Receive the event from the 'withdrawCourse()' function and update DailyPlan UI**/
    private fun subscribeToCourseWithdrawnEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                EventBus.courseWithdrawnEvent.collect {
                    viewModel.checkIfStudentEnrolledInAnyCourse()
                    EventBus.lessonCompletedEvent.resetReplayCache()
                }
            }
        }
    }

    private fun showPopup(anchorView: View, text: String) {
        // Inflate the popup layout
        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_info, null, false)

        // Set the popup text
        val popupText = popupView.findViewById<TextView>(R.id.popup_text)
        popupText.text = text

        // Create the popup window
        val popupWindow = PopupWindow(popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        // Show the popup window
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.height - popupView.height)

        // Dismiss the popup window after 2 seconds (2000 milliseconds)
        Handler(Looper.getMainLooper()).postDelayed({ popupWindow.dismiss() }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), com.maverkick.common.R.color.main_color)
        _binding = null
    }
}
