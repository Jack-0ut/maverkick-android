package com.example.auth.onboarding.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.auth.databinding.FragmentExpertiseBinding
import com.example.auth.onboarding.teacher.TeacherOnboardingViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of TeacherOnboarding, where user chooses
 * the areas of expertise he/she would like to teach
 ***/
@AndroidEntryPoint
class ExpertiseFragment : Fragment() {
    private var _binding: FragmentExpertiseBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: TeacherOnboardingViewModel by activityViewModels()
    private val selectedExpertiseAreas = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpertiseBinding.inflate(inflater, container, false)
        //TODO we need to get this list from somewhere
        val expertiseList = listOf("Psychology", "Solar Engineering", "Math","Mental Health","Quantum Mechanics","Digital Marketing") // replace this with your actual skills
        for (expertise in expertiseList) {
            val chip = Chip(context)
            chip.text = expertise
            chip.isClickable = true
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedExpertiseAreas.add(expertise)
                } else {
                    selectedExpertiseAreas.remove(expertise)
                }
            }
            binding.expertiseChipGroup.addView(chip)
        }

        return binding.root
    }


    fun onNextClicked() {
        onboardingViewModel.expertiseList.value = selectedExpertiseAreas
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
