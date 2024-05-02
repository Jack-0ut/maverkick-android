package com.maverkick.auth.onboarding.student.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.maverkick.auth.databinding.FragmentDailyStudyBinding
import com.maverkick.auth.onboarding.student.StudentOnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of StudentOnboarding, where user
 * chooses the minutes, he'd like to spend studying every day
 **/
@AndroidEntryPoint
class DailyStudyFragment : Fragment() {
    private var _binding: FragmentDailyStudyBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: StudentOnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyStudyBinding.inflate(inflater, container, false)
        binding.dailyStudyMinutesCounterView.stepSize = 5
        return binding.root
    }

    fun onNextClicked() {
        val dailyLearningTime = binding.dailyStudyMinutesCounterView.value
        onboardingViewModel.dailyLearningTime.value = dailyLearningTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
