package com.example.student.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.student.adapters.CourseAdapter
import com.example.student.databinding.FragmentGalleryBinding
import com.example.student.viewmodels.GalleryViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment that is responsible for the Gallery Menu Item (Student)
 * Here student could search for the courses he'd like to try
 **/
//TODO add courses when user clicks on the Gallery Icon automatically
@AndroidEntryPoint
class GalleryFragment : Fragment() {
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

        val adapter = CourseAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            adapter.submitList(courses)
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

        binding.searchButton.setOnClickListener {
            viewModel.searchCourses(viewModel.searchQuery.value ?: "")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
