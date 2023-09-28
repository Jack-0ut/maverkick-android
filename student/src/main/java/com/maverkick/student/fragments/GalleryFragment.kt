package com.maverkick.student.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.models.Course
import com.maverkick.data.sharedpref.SharedPrefManager
import com.maverkick.student.adapters.CourseOverviewAdapter
import com.maverkick.student.adapters.SearchCourseAdapter
import com.maverkick.student.course.CourseDetailsActivity
import com.maverkick.student.databinding.FragmentGalleryBinding
import com.maverkick.student.viewmodels.GalleryViewModel
import com.maverkick.text_lesson.ui.CreatePersonalizedCourseActivity
import com.maverkick.text_lesson.ui.TextCourseOverviewActivity
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
                viewModel.fetchGeneratedTextCourses()
                viewModel.clearNewDataFlag()
                hasFetchedDataInCurrentLifecycle = true
            }
        }

        viewModel.checkForNewData()

        // Adapter for unenrolled generated courses
        val generatedCourseAdapter = CourseOverviewAdapter(object : OnItemClickListener<Course> {
            override fun onItemClick(item: Course) {
                val intent = Intent(requireContext(), TextCourseOverviewActivity::class.java)
                intent.putExtra("courseId", item.courseId)
                startActivity(intent)
            }
        })

        // Observe the generated courses LiveData for unenrolled generated courses
        viewModel.generatedCourses.observe(viewLifecycleOwner) { courses ->
            if (courses.isEmpty()) {
                binding.generatedByYouLabel.visibility = View.GONE
            } else {
                binding.generatedByYouLabel.visibility = View.VISIBLE
            }
            generatedCourseAdapter.submitList(courses)
        }

        // Set up the RecyclerView for unenrolled generated courses with horizontal scrolling
        binding.generatedCoursesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.generatedCoursesList.adapter = generatedCourseAdapter

        // Instantiate the adapter
        val adapter = SearchCourseAdapter { clickedCourse ->
            val intent = Intent(requireContext(), CourseDetailsActivity::class.java)
            intent.putExtra("courseId", clickedCourse.courseId)
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

        binding.recommendedCoursesList.layoutManager = LinearLayoutManager(context)
        binding.recommendedCoursesList.adapter = adapter

        // Observe the courses LiveData from the ViewModel
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            adapter.submitList(courses)
        }

        // navigate to the course generation activity
        binding.courseGenerationButton.setOnClickListener {
            val intent = Intent(requireContext(), CreatePersonalizedCourseActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if (!hasFetchedDataInCurrentLifecycle) {
            Log.d("GalleryFragment", "Checking for new data since it hasn't been fetched in this lifecycle yet")
            viewModel.checkForNewData()
        } else {
            Log.d("GalleryFragment", "Data already fetched in this lifecycle. Not checking again.")
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
