package com.maverkick.teacher.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.maverkick.teacher.adapters.CourseNameAdapter
import com.maverkick.teacher.adapters.CourseStatisticsAdapter
import com.maverkick.teacher.databinding.FragmentTeacherProfileStatisticsBinding
import com.maverkick.teacher.viewmodels.TeacherProfileStatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Teacher Profile sub-fragment, where the course statistics gonna be displayed
 **/
@AndroidEntryPoint
class TeacherProfileStatisticsFragment : Fragment() {
    private val viewModel: TeacherProfileStatisticsViewModel by viewModels()
    private lateinit var courseStatisticsAdapter: CourseStatisticsAdapter
    private lateinit var courseNameAdapter: CourseNameAdapter

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

        // Attach CourseNameAdapter for course names
        courseNameAdapter = CourseNameAdapter { position ->
            // Click listener to handle course selection
            binding.statisticsViewPager.setCurrentItem(position, true)
        }
        binding.coursesRecyclerView.adapter = courseNameAdapter

        // Attach CourseStatisticsAdapter for course statistics
        courseStatisticsAdapter = CourseStatisticsAdapter()
        binding.statisticsViewPager.adapter = courseStatisticsAdapter

        // Observe the LiveData from ViewModel
        viewModel.courseStatistics.observe(viewLifecycleOwner) { courses ->
            val courseList = courses.values.toList()
            courseNameAdapter.submitList(courseList)
            courseStatisticsAdapter.submitList(courseList)

            // Handle arrow visibility based on course count
            binding.leftArrow.visibility = View.INVISIBLE // Always hide left arrow at the start
            binding.rightArrow.visibility = if (courseList.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

        // Observe error messages from ViewModel
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showSnackbar(it)
            }
        }

        setupCourseNameSync()
        setupArrowsNavigation()
    }

    private fun setupCourseNameSync() {
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.coursesRecyclerView)

        binding.coursesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val pos = getCurrentCenterPosition()

                    // Handle arrow visibility based on the centered position
                    binding.leftArrow.visibility = if (pos <= 0) View.INVISIBLE else View.VISIBLE
                    binding.rightArrow.visibility = if (pos >= courseNameAdapter.itemCount - 1) View.INVISIBLE else View.VISIBLE
                }
            }
        })
    }

    private fun setupArrowsNavigation() {
        binding.leftArrow.setOnClickListener {
            val currentPosition = getCurrentCenterPosition()
            if (currentPosition > 0) { // Check so we don't go below the first item
                val newPosition = currentPosition - 1
                binding.coursesRecyclerView.smoothScrollToPosition(newPosition)
                binding.statisticsViewPager.setCurrentItem(newPosition, true)
            }
        }

        binding.rightArrow.setOnClickListener {
            val currentPosition = getCurrentCenterPosition()
            if (currentPosition < courseNameAdapter.itemCount - 1) { // Check so we don't exceed the last item
                val newPosition = currentPosition + 1
                binding.coursesRecyclerView.smoothScrollToPosition(newPosition)
                binding.statisticsViewPager.setCurrentItem(newPosition, true)
            }
        }
    }

    private fun getCurrentCenterPosition(): Int {
        val snapHelper = LinearSnapHelper()
        val centerView = snapHelper.findSnapView(binding.coursesRecyclerView.layoutManager)
        return binding.coursesRecyclerView.layoutManager?.getPosition(centerView!!) ?: -1
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        } ?: run {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
