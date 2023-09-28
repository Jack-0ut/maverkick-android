package com.maverkick.student.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.models.Course
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.student.adapters.CourseAdapter
import com.maverkick.student.databinding.FragmentStudentProfileCoursesBinding
import com.maverkick.student.viewmodels.StudentProfileCoursesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for displaying the Profile -> Courses section.
 * This fragment shows the list of courses that a student is currently enrolled in,
 * and allows the student to un-enroll from a course.
 *
 * Utilizes the [StudentProfileCoursesViewModel] to manage the underlying data.
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

        // Create an instance of the adapter with a lambda expression for the click listener
        val courseAdapter = CourseAdapter(object : OnItemClickListener<Course> {
            override fun onItemClick(item: Course) {
                val courseType = when(item) {
                    is VideoCourse -> CourseType.VIDEO
                    is TextCourse -> CourseType.TEXT
                    else -> throw IllegalArgumentException("Unknown course type")
                }
                viewModel.withdrawFromCourse(item.courseId, courseType)
            }
        })

        // Set the layout manager and adapter for the RecyclerView
        binding.coursesList.layoutManager = LinearLayoutManager(context)
        binding.coursesList.adapter = courseAdapter

        // Observe the merged courses LiveData
        viewModel.currentCourses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.submitList(courses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
