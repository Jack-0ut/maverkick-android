package com.example.student.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.models.Lesson
import com.example.student.adapters.LessonAdapter
import com.example.student.databinding.FragmentStudentHomeBinding
import com.example.student.videolesson.VideoLessonActivity
import com.example.student.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Fragment for the Home Menu Item (Student)
 * It responsible for displaying today's lessons
 **/
@AndroidEntryPoint
class StudentHomeFragment : Fragment(),LessonAdapter.OnLessonClickListener {
    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

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
        // if student doesn't have courses yet, recommend us go to the Gallery to choose new/first course
        viewModel.navigateToCourseEnrollment.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                val action =
                    StudentHomeFragmentDirections.actionStudentHomeFragmentToGalleryFragment()
                findNavController().navigate(action)
                viewModel.onCourseEnrollmentNavigationComplete()
            }
        }

        // if it's the last lesson, remove button
        viewModel.isLastLesson.observe(viewLifecycleOwner) { isLast ->
            if (isLast) {
                // Hide the button
                binding.startLearningButton.visibility = View.GONE
            } else {
                // Show the button
                binding.startLearningButton.visibility = View.VISIBLE
            }
        }

        // click on the "Start Learning" button
        binding.startLearningButton.setOnClickListener {
            // Get the current lesson using the currentLessonIndex
            val currentLessonIndex = viewModel.currentLessonIndex.value ?: 0
            val currentLesson = viewModel.dailyLearningPlan.value?.lessons?.get(currentLessonIndex)

            currentLesson?.let { lesson ->
                val intent = Intent(context, VideoLessonActivity::class.java)
                intent.putExtra("lessonId", lesson.lessonId)
                intent.putExtra("videoUri", lesson.videoUrl)
                intent.putExtra("transcription", lesson.transcription)
                intent.putExtra("title", lesson.title)
                intent.putExtra("courseId", lesson.courseId)
                startActivity(intent)
            }
        }

        // Init RecyclerView and pass the list of lessons that Student should learn today
        val initialIndex = viewModel.currentLessonIndex.value ?: 0
        val lessonsAdapter = LessonAdapter(this, initialIndex)
        binding.lessonsRecyclerView.adapter = lessonsAdapter
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(context)

        // observe the current lesson
        viewModel.currentLessonIndex.observe(viewLifecycleOwner) { newCurrentLessonIndex ->
            lessonsAdapter.updateCurrentLessonIndex(newCurrentLessonIndex)
        }

        // Observe the changes to the dailyLearningPlan
        viewModel.dailyLearningPlan.observe(viewLifecycleOwner) { dailyLearningPlan ->
            // Extract the list of lessons from the DailyLearningPlan
            lessonsAdapter.submitList(dailyLearningPlan.lessons)
        }

        // Observe the bricksCollected
        viewModel.bricksCollected.observe(viewLifecycleOwner) { bricks ->
            binding.pointsNumber.text = bricks.toString()
        }

        // when lesson completed broadcast is received
        val lessonCompletedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val lessonId = intent.getStringExtra("lessonId")
                val courseId = intent.getStringExtra("courseId")
                // Call the ViewModel function to update student learning progress
                if (lessonId != null && courseId != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.updateStudentLearningProgress(lessonId, courseId)
                    }
                }
            }
        }

        // Register the receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            lessonCompletedReceiver,
            IntentFilter("LESSON_COMPLETED_ACTION")
        )
    }

    /** Click on the particular lesson **/
    override fun onLessonClick(lesson: Lesson) {
        val intent = Intent(context, VideoLessonActivity::class.java)
        intent.putExtra("lessonId", lesson.lessonId)
        intent.putExtra("videoUri", lesson.videoUrl)
        intent.putExtra("transcription", lesson.transcription)
        intent.putExtra("title", lesson.title)
        intent.putExtra("courseId", lesson.courseId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
