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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.teacher.R
import com.example.teacher.databinding.FragmentAddCourseBinding
import com.example.teacher.viewmodels.AddCourseViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment, where Teacher would add information
 * about new course like courseName, language, tags and maybe something more
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

        // Add new Course button click
        binding.submitButton.setOnClickListener {
            // Ask the ViewModel to handle course submission
            viewModel.submitCourse { success, courseId ->
                if (success) {
                    // If course was added successfully, navigate to the EditCourseFragment
                    // Use the Navigation component to navigate and pass the courseId as an argument
                    val action = AddCourseFragmentDirections.actionAddCourseFragmentToEditCourseFragment(courseId)
                    findNavController().navigate(action)
                } else {
                    // Handle error case
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.courseName.collect { name ->
                    if (name != binding.courseName.text.toString()) {
                        binding.courseName.setText(name)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLanguage.collect { language ->
                    if (language != (binding.courseLanguage.editText as? AutoCompleteTextView)?.text.toString()) {
                        (binding.courseLanguage.editText as? AutoCompleteTextView)?.setText(language)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tags.collect { tags ->
                    if (tags != binding.tagInputField.getCurrentTags()) {
                        binding.tagInputField.setTags(tags)
                    }
                }
            }
        }

        // When changing the name of the course
        binding.courseName.doAfterTextChanged { text ->
            viewModel.updateCourseName(text.toString())
        }

        // When changing the language of course
        (binding.courseLanguage.editText as? AutoCompleteTextView)?.doAfterTextChanged { text ->
            viewModel.updateLanguage(text.toString())
        }

        // When we add new tag
        binding.tagInputField.onTagAdded = { tag ->
            if (!viewModel.addTag(tag)) {
                val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.tagInputField.windowToken, 0)
                Snackbar.make(binding.tagsLabel, "You have reached the limit of 5 tags.", Snackbar.LENGTH_SHORT).show()
            }
        }
        // When we remove the tag
        binding.tagInputField.onTagRemoved = { tag ->
            viewModel.removeTag(tag)
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
