package com.maverkick.student.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.maverkick.student.adapters.CourseAdapter
import com.maverkick.student.adapters.OnCourseInteractionListener
import com.maverkick.student.databinding.FragmentStudentProfileCoursesBinding
import com.maverkick.student.viewmodels.StudentProfileCoursesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for the Profile-> Courses,
 * where we display the list of courses Student
 * currently taking
 **/
@AndroidEntryPoint
class StudentProfileCoursesFragment : Fragment() {
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
        val adapter = CourseAdapter(object : OnCourseInteractionListener {
            override fun onLeaveCourse(courseId: String) {
                viewModel.withdrawFromCourse(courseId)
            }
        })

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
