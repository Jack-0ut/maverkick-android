package com.example.student.fragments.profile_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.student.adapters.CourseAdapter
import com.example.student.databinding.FragmentStudentProfileCoursesBinding
import com.example.student.viewmodels.StudentProfileCoursesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for the Profile-> Courses,
 * where we display the list of courses Student
 * currently taking
 **/
@AndroidEntryPoint
class StudentProfileCoursesFragment : Fragment() {

    // Get a reference to the ViewModel
    private val viewModel: StudentProfileCoursesViewModel by viewModels()

    private var _binding: FragmentStudentProfileCoursesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentProfileCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create an instance of the adapter
        val adapter = CourseAdapter()

        // Set the layout manager and adapter for the RecyclerView
        binding.coursesList.layoutManager = LinearLayoutManager(context)
        binding.coursesList.adapter = adapter

        // Observe the courses LiveData
        viewModel.currentCourses.observe(viewLifecycleOwner) { courses ->
            // Update the RecyclerView's data when the courses LiveData updates
            adapter.submitList(courses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
