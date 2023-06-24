package com.example.teacher.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.teacher.adapters.LessonAdapter
import com.example.teacher.databinding.FragmentCourseEditBinding
import com.example.teacher.viewmodels.EditCourseViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment, where Teacher will be interacting with the course,
 * updating some things, uploading new videos and so on
 **/
@AndroidEntryPoint
class EditCourseFragment : Fragment(),LessonAdapter.OnLessonClickListener {
    private var _binding: FragmentCourseEditBinding? = null
    private val binding get() = _binding!!

    private val args: EditCourseFragmentArgs by navArgs()
    private val viewModel: EditCourseViewModel by viewModels()

    private lateinit var courseId: String
    private val lessonAdapter = LessonAdapter()

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

        // Fetch the necessary data from the database
        viewModel.fetchCourse(courseId)
        viewModel.fetchLessons(courseId)

        // Set up RecyclerView
        binding.lessonList.layoutManager = LinearLayoutManager(context)
        binding.lessonList.adapter = lessonAdapter
        lessonAdapter.setOnLessonClickListener(this)

        viewModel.course.observe(viewLifecycleOwner) { course ->
            // Update course related UI
            binding.courseName.text = course.courseName

            Glide.with(this).load(course.poster).into(binding.coursePoster)

            // Clear old chips if any before adding new ones
            binding.tags.removeAllViews()

            // Create a new Chip for each tag and add it to the ChipGroup
            course.tags.forEach { tag ->
                val chip = Chip(context)
                chip.text = tag
                binding.tags.addView(chip)
            }
        }

        // Observe the posterUri LiveData
        viewModel.posterUri.observe(viewLifecycleOwner) { uri ->
            // Load the poster image from the updated Uri
            Glide.with(this).load(uri).into(binding.coursePoster)
        }

        viewModel.lessons.observe(viewLifecycleOwner) { lessons ->
            lessonAdapter.submitList(lessons)
        }

        binding.addLessonButton.setOnClickListener {
            viewModel.course.value?.let { course ->
                val action = EditCourseFragmentDirections.actionEditCourseFragmentToSelectVideoFragment(courseId, course.language)
                findNavController().navigate(action)
            } ?: run {
                // Handle the error if the course is null. For example, show an error message to the user.
            }
        }

        binding.editPosterIcon.setOnClickListener{
            getContent.launch("image/*")
        }
    }

    /** get new poster from gallery */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePoster(uri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** When click on the particular lesson toggle icon **/
    override fun onLessonClick(lessonId: String, position: Int) {
        lessonAdapter.onLessonClick(lessonId, position)
    }
}
