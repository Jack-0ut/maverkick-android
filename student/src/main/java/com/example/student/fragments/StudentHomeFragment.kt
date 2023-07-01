package com.example.student.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.models.Lesson
import com.example.student.adapters.LessonAdapter
import com.example.student.adapters.OnLessonClickListener
import com.example.student.databinding.FragmentStudentHomeBinding
import com.example.student.videolesson.VideoLessonActivity
import com.example.student.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * The Fragment for the Home Menu Item (Student)
 * It responsible for displaying today's lessons
 **/
@AndroidEntryPoint
class StudentHomeFragment : Fragment(), OnLessonClickListener {
    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels() // Create an instance of HomeViewModel

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
        // Here you can initialize your RecyclerView and Button. For example:
        binding.startLearningButton.setOnClickListener {
            // Handle button click
        }

        // Init RecyclerView and pass the list of lessons, that Student should learn today
        val lessonsAdapter = LessonAdapter(this)
        binding.lessonsRecyclerView.adapter = lessonsAdapter
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Observe the changes to the list, if happens it will be automatically updated
        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            lessonsAdapter.submitList(lessons)
        }

    }
    /** Click on the particular lesson **/
    override fun onLessonClick(lesson: Lesson) {
        val intent = Intent(context, VideoLessonActivity::class.java)
        intent.putExtra("lessonId", lesson.lessonId)
        intent.putExtra("videoUri", lesson.videoUrl)
        intent.putExtra("transcription", lesson.transcription)
        intent.putExtra("title", lesson.title)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
