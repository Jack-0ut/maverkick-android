package com.example.auth.onboarding.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.auth.databinding.FragmentFullNameBinding
import com.example.auth.onboarding.teacher.TeacherOnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of the TeacherOnboarding, where user should enter his name,
 * it's important since we should have a way to talk to the teacher using that name
 * and also this is the part of the identity confirmation
 **/
@AndroidEntryPoint
class FullNameFragment: Fragment() {
    private var _binding: FragmentFullNameBinding? = null
    private val binding get() = _binding!!
    private val onboardingViewModel: TeacherOnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun onNextClicked() {
        val fullName = binding.fullNameInput.text.toString()
        onboardingViewModel.fullName.value = fullName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
