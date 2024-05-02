package com.maverkick.auth.onboarding.student.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.maverkick.auth.databinding.FragmentInterestsBinding
import com.maverkick.auth.onboarding.student.StudentOnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of StudentOnboarding, where user chooses
 * the skills he's interested in or would like to learn
 **/
@AndroidEntryPoint
class InterestsFragment : Fragment() {
    private var _binding: FragmentInterestsBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: StudentOnboardingViewModel by activityViewModels()
    private val selectedInterests = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInterestsBinding.inflate(inflater, container, false)

        val interests = listOf(
            "Sciences",
            "Engineering & Technology",
            "Arts & Humanities",
            "Social Sciences",
            "Business",
            "Health & Medicine",
            "Education",
            "Environmental Studies",
            "Media & Communication"
        )

        for (interest in interests) {
            val chip = Chip(context, null, com.maverkick.common.R.style.InterestChipStyle)
            chip.text = interest
            chip.isClickable = true
            chip.isCheckable = true
            chip.isCloseIconVisible = false
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedInterests.add(interest)
                } else {
                    selectedInterests.remove(interest)
                }
            }
            binding.interestsChipGroup.addView(chip)
        }

        return binding.root
    }

    fun onNextClicked() {
        onboardingViewModel.interests.value = selectedInterests
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
