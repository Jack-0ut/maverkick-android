package com.example.auth.onboarding.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.auth.databinding.FragmentCountryBinding
import com.example.auth.onboarding.teacher.TeacherOnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryFragment: Fragment() {
    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: TeacherOnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun onNextClicked() {
        val countryName = binding.country.selectedCountryName
        onboardingViewModel.country.value = countryName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
