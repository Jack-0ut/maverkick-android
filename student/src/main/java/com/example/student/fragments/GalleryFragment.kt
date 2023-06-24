package com.example.student.fragments

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.student.adapters.OnSearchCourseClickListener
import com.example.student.adapters.SearchCourseAdapter
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

        val adapter = SearchCourseAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel.searchHits.observe(viewLifecycleOwner) { courses ->
            if (courses.isNullOrEmpty()) {
                binding.noResultsTextView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.noResultsTextView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(courses)
            }
        }

        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchQuery.value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // click on the search icon button
        binding.searchButton.setOnClickListener {
            viewModel.searchCourses(viewModel.searchQuery.value ?: "")
            hideKeyboard(requireActivity())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** When student clicks on the particular course */
    override fun onSearchCourseClick(courseId: String) {
        val action = GalleryFragmentDirections.actionToCourseDetailsFragment(courseId)
        findNavController().navigate(action)
    }

    /** Hide the keyboard */
    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
