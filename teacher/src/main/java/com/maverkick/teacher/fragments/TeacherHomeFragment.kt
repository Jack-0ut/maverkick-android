package com.maverkick.teacher.fragments

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
import com.example.shared_ui.OnItemClickListener
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.Course
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.teacher.adapters.CourseAdapter
import com.maverkick.teacher.databinding.FragmentTeacherHomeBinding
import com.maverkick.teacher.edit_course.text.EditTextCourseActivity
import com.maverkick.teacher.edit_course.video.EditVideoCourseActivity
import com.maverkick.teacher.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * The Fragment for the Home Menu Item (Teacher)
 * It responsible for displaying the courses teacher published
 * with ability to edit/expand the course
 **/
@AndroidEntryPoint
class TeacherHomeFragment : Fragment() {
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

        val courseAdapter = CourseAdapter(object : OnItemClickListener<Course> {
            override fun onItemClick(item: Course) {

                val intent: Intent = when (item) {
                    is VideoCourse -> {
                        Intent(activity, EditVideoCourseActivity::class.java)
                    }
                    is TextCourse -> {
                        Intent(activity, EditTextCourseActivity::class.java)
                    }
                    else -> {
                        // Handle unknown Course type here, maybe throw an error or log it
                        return
                    }
                }

                intent.putExtra("courseId", item.courseId)
                startActivity(intent)
            }
        })

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
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            showSnackbar(errorMessage)
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
