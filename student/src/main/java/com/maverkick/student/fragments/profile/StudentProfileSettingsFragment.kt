package com.maverkick.student.fragments.profile

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.maverkick.common.CounterView
import com.maverkick.student.databinding.FragmentStudentProfileSettingsBinding
import com.maverkick.student.viewmodels.StudentProfileSettingsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
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
                    chip.isCheckable = false
                    chip.isCloseIconVisible = false
                    chip.text = interest
                    binding.interestsChipGroup.addView(chip)
                }
            }
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }


        // When the daily learning time value TextView is clicked
        binding.dailyLearningTimeValue.setOnClickListener {
            // Create a new instance of CounterView
            val counterView = CounterView(requireContext(), null).apply {
                minValue = 10
                maxValue = 30
                value = binding.dailyLearningTimeValue.text.toString().toInt()
                stepSize = 5

                // Color changes
                val myColor = ContextCompat.getColor(context, com.maverkick.common.R.color.black)
                minusIcon.setColorFilter(myColor, PorterDuff.Mode.SRC_IN)
                plusIcon.setColorFilter(myColor, PorterDuff.Mode.SRC_IN)
                valueTextView.setTextColor(myColor)
            }

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Set Daily Learning Time")
                .setView(counterView) // Set the CounterView as the content of the dialog
                .setPositiveButton("OK") { _, _ ->
                    // When the OK button is clicked, update the daily learning time in the ViewModel
                    viewModel.updateDailyLearningTime(counterView.value.toString())
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
