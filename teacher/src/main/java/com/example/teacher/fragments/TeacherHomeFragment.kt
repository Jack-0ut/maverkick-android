package com.example.teacher.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teacher.adapters.CourseAdapter
import com.example.teacher.databinding.FragmentTeacherHomeBinding
import com.example.teacher.edit_course.EditCourseActivity
import com.example.teacher.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The Fragment for the Home Menu Item (Teacher)
 * It responsible for displaying the courses teacher published
 * with ability to edit/expand the course
 **/
@AndroidEntryPoint
class TeacherHomeFragment : Fragment(), CourseAdapter.OnCourseClickListener{
    private var _binding: FragmentTeacherHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your CourseAdapter
        val courseAdapter = CourseAdapter(this)

        // Set the LayoutManager of your RecyclerView
        binding.teacherCourses.layoutManager = LinearLayoutManager(context)

        // Set the adapter of your RecyclerView
        binding.teacherCourses.adapter = courseAdapter

        // Observe the changes to the list, if happens it will be automatically updated
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.courses.collect { courses ->
                    courseAdapter.submitList(courses)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** When click on the particular course edit icon, redirect to the EditCourseFragment for that course**/
    override fun onCourseClick(courseId: String) {
        val intent = Intent(activity, EditCourseActivity::class.java)
        intent.putExtra("courseId", courseId)
        startActivity(intent)
    }
}
