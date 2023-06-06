package com.example.teacher.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teacher.databinding.FragmentTeacherProfileSettingsBinding
import com.example.teacher.viewmodels.TeacherProfileSettingsViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Teacher Profile sub-fragment, which gonna be displaying the teacher's area of expertise
 **/
@AndroidEntryPoint
class TeacherProfileSettingsFragment : Fragment() {

    private val viewModel: TeacherProfileSettingsViewModel by viewModels()

    private var _binding: FragmentTeacherProfileSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherProfileSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the expertise LiveData
        viewModel.expertiseList.observe(viewLifecycleOwner) { expertise ->
            expertise?.let {
                for (i in it) {
                    val chip = Chip(context)
                    chip.text = i
                    binding.expertiseList.addView(chip)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
