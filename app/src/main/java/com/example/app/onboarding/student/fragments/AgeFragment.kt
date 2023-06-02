package com.example.app.onboarding.student.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.app.databinding.FragmentAgeBinding
import com.example.app.onboarding.student.StudentOnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of the StudentOnboarding, where
 * user should choose the age
 **/
@AndroidEntryPoint
class AgeFragment : Fragment() {
    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: StudentOnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun onNextClicked() {
        val dailyLearningTime = binding.ageCounterView.value
        onboardingViewModel.age.value = dailyLearningTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
