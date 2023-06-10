package com.example.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.teacher.databinding.FragmentCourseEditBinding
import com.example.teacher.viewmodels.EditCourseViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment, where Teacher will be interacting with the course,
 * updating some things, uploading new videos and so on
 **/
@AndroidEntryPoint
class EditCourseFragment : Fragment() {
    private var _binding: FragmentCourseEditBinding? = null
    private val binding get() = _binding!!

    private val args: EditCourseFragmentArgs by navArgs()
    private lateinit var courseId: String

    private val viewModel: EditCourseViewModel by viewModels() // If using Hilt, use "by hiltNavGraphViewModels(R.id.nav_graph)"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseEditBinding.inflate(inflater, container, false)

        courseId = args.courseId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchCourse(courseId)
        viewModel.fetchLessons(courseId)

        viewModel.course.observe(viewLifecycleOwner) { course ->

            // Update course related UI...
            binding.courseName.text = course.courseName

            // Set image for the course poster using a library like Glide or Picasso
            // Glide.with(this).load(course.poster).into(binding.coursePoster)

            // Clear old chips if any before adding new ones
            binding.tags.removeAllViews()

            // Create a new Chip for each tag and add it to the ChipGroup
            course.tags.forEach { tag ->
                val chip = Chip(context)
                chip.text = tag
                binding.tags.addView(chip)
            }
        }

        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            // Update lessons list UI...
            // Here you will likely bind your RecyclerView adapter with the fetched lessons
        }

        binding.addLessonButton.setOnClickListener{

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
