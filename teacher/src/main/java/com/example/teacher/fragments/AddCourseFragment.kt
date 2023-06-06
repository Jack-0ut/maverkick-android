package com.example.teacher.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.teacher.R
import com.example.teacher.databinding.FragmentAddCourseBinding
import com.example.teacher.viewmodels.AddCourseViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment, where Teacher would add information
 * about new course
 **/
@AndroidEntryPoint
class AddCourseFragment : Fragment() {
    private var _binding: FragmentAddCourseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddCourseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCourseBinding.inflate(inflater, container, false)

        initializeTagInputField()
        initializeDropdown()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener {

            // Collect data from UI
            val courseName = binding.courseName.text.toString()
            val selectedLanguage = (binding.courseLanguage.editText as? AutoCompleteTextView)?.text.toString()

            val tags = mutableListOf<String>()
            for (i in 0 until binding.tagInputField.chipGroup.childCount) {
                val chip = binding.tagInputField.chipGroup.getChildAt(i) as? Chip
                chip?.let { tags.add(it.text.toString()) }
            }

            // Pass data to ViewModel
            viewModel.updateCourseName(courseName)
            viewModel.updateTags(tags)
            viewModel.updateLanguage(selectedLanguage)

            // Ask the ViewModel to handle course submission
            viewModel.submitCourse()
        }


        binding.tagInputField.onMaxChipsReached = {
            // Hide keyboard
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.tagInputField.windowToken, 0)
            Snackbar.make(binding.tagsLabel, "You could enter up to 5 tags", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeTagInputField(){
        binding.tagInputField.tagInputEditText.hint = "Enter the tag and hit on space"
        binding.tagInputField.tagInputEditText.setBackgroundResource(R.drawable.tag_input_rounded_corners)
        binding.tagInputField.tagInputEditText.setBackgroundColor(ContextCompat.getColor(requireContext(), com.example.common.R.color.accent_color))
    }

    private fun initializeDropdown() {
        val languages = resources.getStringArray(R.array.languages)
        val adapter = ArrayAdapter(requireContext(), R.layout.language_list_item, languages)
        (binding.courseLanguage.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

}
