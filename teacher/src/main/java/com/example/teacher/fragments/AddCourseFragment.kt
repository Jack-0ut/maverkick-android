package com.example.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teacher.databinding.FragmentAddCourseBinding
import com.example.teacher.viewmodels.AddCourseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddCourseFragment : Fragment() {
    private var _binding: FragmentAddCourseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddCourseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
