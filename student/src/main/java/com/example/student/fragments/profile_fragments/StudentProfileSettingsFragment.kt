package com.example.student.fragments.profile_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.student.databinding.FragmentStudentProfileSettingsBinding
import com.example.student.viewmodels.StudentProfileSettingsViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for the Profile-> Settings,
 * where we display the dailyLearningTime and interests of the Student
 **/
@AndroidEntryPoint
class StudentProfileSettingsFragment : Fragment() {
    private var _binding: FragmentStudentProfileSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StudentProfileSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentProfileSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the dailyLearningTime LiveData
        viewModel.dailyLearningTime.observe(viewLifecycleOwner) { dailyLearningTime ->
            binding.dailyLearningTimeValue.text = dailyLearningTime
        }

        // Observe the interests LiveData
        viewModel.interests.observe(viewLifecycleOwner) { interests ->
            interests?.let {
                for (interest in it) {
                    val chip = Chip(context)
                    chip.text = interest
                    binding.interestsChipGroup.addView(chip)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}