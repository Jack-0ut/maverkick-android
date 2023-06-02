package com.example.app.onboarding.student.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.app.databinding.FragmentSkillsBinding
import com.example.app.onboarding.student.StudentOnboardingViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of StudentOnboarding, where user chooses
 * the skills he's interested in or would like to learn
 **/
@AndroidEntryPoint
class SkillsFragment : Fragment() {
    private var _binding: FragmentSkillsBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: StudentOnboardingViewModel by activityViewModels()
    private val selectedSkills = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsBinding.inflate(inflater, container, false)
        //TODO we need to get this list from somewhere
        val skills = listOf("Psychology", "Solar Engineering", "Math","Mental Health","Quantum Mechanics","Digital Marketing") // replace this with your actual skills
        for (skill in skills) {
            val chip = Chip(context)
            chip.text = skill
            chip.isClickable = true
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedSkills.add(skill)
                } else {
                    selectedSkills.remove(skill)
                }
            }
            binding.skillsChipGroup.addView(chip)
        }

        return binding.root
    }


    fun onNextClicked() {
        onboardingViewModel.skills.value = selectedSkills
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
