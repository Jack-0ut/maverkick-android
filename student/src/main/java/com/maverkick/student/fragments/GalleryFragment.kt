package com.maverkick.student.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.maverkick.data.models.CourseType
import com.maverkick.data.sharedpref.SharedPrefManager
import com.maverkick.student.adapters.CourseGridAdapter
import com.maverkick.student.course.CourseOverviewActivity
import com.maverkick.student.databinding.FragmentGalleryBinding
import com.maverkick.student.viewmodels.GalleryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment that is responsible for the Gallery Menu Item (Student)
 * Here student could search for the courses he'd like to try
 **/
@AndroidEntryPoint
class GalleryFragment : Fragment(){
    private val viewModel: GalleryViewModel by viewModels()

    private var hasFetchedDataInCurrentLifecycle = false
    @Inject lateinit var sharedPrefManager: SharedPrefManager
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isNewDataAvailable.observe(viewLifecycleOwner) { isNewData ->
            if (isNewData && !hasFetchedDataInCurrentLifecycle) {
                viewModel.decrementCourseGenerationTries()
                //viewModel.fetchGeneratedTextCourses()
                viewModel.clearNewDataFlag()
                hasFetchedDataInCurrentLifecycle = true
            }
        }

        viewModel.checkForNewData()

        val adapter = CourseGridAdapter { clickedCourse ->
            val intent = when (clickedCourse.type) {
                CourseType.VIDEO -> {
                    Intent(requireContext(), CourseOverviewActivity::class.java)
                }
                CourseType.TEXT, CourseType.TEXT_PERSONALIZED -> {
                    Intent(requireContext(), CourseOverviewActivity::class.java)
                }
            }
            intent.putExtra("courseId", clickedCourse.courseId)
            intent.putExtra("courseType", clickedCourse.type.name)
            startActivity(intent)
        }

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            if (courses.isEmpty()) {
                binding.recommendedHeadline.visibility = View.GONE
            } else {
                binding.recommendedHeadline.visibility = View.VISIBLE
            }
            adapter.submitList(courses)
        }

        binding.recommendedCoursesList.layoutManager = GridLayoutManager(context, 2) // Set GridLayoutManager with 2 columns
        binding.recommendedCoursesList.adapter = adapter

        // Observe the courses LiveData from the ViewModel
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            adapter.submitList(courses)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasFetchedDataInCurrentLifecycle) {
            viewModel.checkForNewData()
        }
    }

    override fun onPause() {
        super.onPause()
        hasFetchedDataInCurrentLifecycle = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
