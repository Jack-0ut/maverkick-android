package com.example.student.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.student.adapters.CourseLessonAdapter
import com.example.student.databinding.FragmentCourseDetailsBinding
import com.example.student.viewmodels.CourseDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment that display the information about the course to the Student
 **/
@AndroidEntryPoint
class CourseDetailsFragment : Fragment() {
    private val viewModel: CourseDetailsViewModel by viewModels()

    private var _binding: FragmentCourseDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val courseId = arguments?.getString("courseId")
        courseId?.let { id ->
            viewModel.fetchCourseDetails(id)
            viewModel.fetchLessons(id)
        }

        // Initialize  CourseLessonAdapter
        val courseLessonAdapter = CourseLessonAdapter()
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.lessonsRecyclerView.adapter = courseLessonAdapter

        // Observe course details LiveData
        viewModel.course.observe(viewLifecycleOwner) { course ->
            // Update UI with course details
            binding.courseTitle.text = course.courseName
        }

        // Observe lessons LiveData
        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            courseLessonAdapter.submitList(lessons)
        }

        // Observe teacher LiveData
        viewModel.teacher.observe(viewLifecycleOwner) { teacher ->
            // Update UI with teacher name
            binding.teacherName.text = teacher.fullName
        }

        // Observe user LiveData
        viewModel.user.observe(viewLifecycleOwner) { user ->
            // Update UI with teacher profile picture
            Glide.with(this).load(user?.profilePicture).into(binding.teacherImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
