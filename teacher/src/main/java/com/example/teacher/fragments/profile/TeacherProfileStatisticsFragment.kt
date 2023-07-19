package com.example.teacher.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teacher.adapters.CourseStatsAdapter
import com.example.teacher.databinding.FragmentTeacherProfileStatisticsBinding
import com.example.teacher.viewmodels.TeacherProfileStatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Teacher Profile sub-fragment, where the course statistics gonna be displayed
 **/
@AndroidEntryPoint
class TeacherProfileStatisticsFragment : Fragment() {

    // Get a reference to the ViewModel
    private val viewModel: TeacherProfileStatisticsViewModel by viewModels()

    // Reference to the adapter
    private lateinit var adapter: CourseStatsAdapter

    private var _binding: FragmentTeacherProfileStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherProfileStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the adapter
        adapter = CourseStatsAdapter()

        // Attach the adapter to the RecyclerView
        binding.coursesRecyclerView.adapter = adapter

        // Observe the LiveData
        viewModel.courseStatistics.observe(viewLifecycleOwner) { courses ->
            // Update the data in the adapter
            adapter.submitList(courses.values.toList())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
