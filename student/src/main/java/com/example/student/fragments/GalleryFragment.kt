package com.example.student.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.student.adapters.OnSearchCourseClickListener
import com.example.student.adapters.SearchCourseAdapter
import com.example.student.course.CourseDetailsActivity
import com.example.student.databinding.FragmentGalleryBinding
import com.example.student.viewmodels.GalleryViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment that is responsible for the Gallery Menu Item (Student)
 * Here student could search for the courses he'd like to try
 **/
@AndroidEntryPoint
class GalleryFragment : Fragment(), OnSearchCourseClickListener {
    private val viewModel: GalleryViewModel by viewModels()
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

        // Instantiate the adapter
        val adapter = SearchCourseAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        // Observe the courses LiveData from the ViewModel
        viewModel.courses.observe(viewLifecycleOwner){ courses ->
            // Submit the list of courses to the adapter
            adapter.submitList(courses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** When student clicks on the particular course */
    override fun onSearchCourseClick(courseId: String) {
        val intent = Intent(requireContext(), CourseDetailsActivity::class.java)
        intent.putExtra("courseId", courseId)
        startActivity(intent)
    }

}
