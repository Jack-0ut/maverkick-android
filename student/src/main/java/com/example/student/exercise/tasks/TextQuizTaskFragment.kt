package com.example.student.exercise.tasks


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.student.databinding.FragmentTextQuizTaskBinding
import com.example.student.exercise.TextQuizTask

/**
 * Fragment for the classical quiz task.
 * This basically takes the quiz and displays it on the screen,
 * checks the right answer
 **/
class TextQuizTaskFragment : Fragment() {

    private var _binding: FragmentTextQuizTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: TextQuizTask

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextQuizTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task = arguments?.getParcelable("task") ?: throw IllegalStateException("Task is missing")

        // set the question and options
        binding.question.text = task.question

        val optionsRadioGroup = binding.options
        task.options.forEachIndexed { index, optionText ->
            val radioButton = binding.options.getChildAt(index) as? RadioButton
            radioButton?.text = optionText
            radioButton?.id = index
        }

        optionsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            checkAnswer(checkedId)
        }
    }

    /**Method that checks the chosen answer and shows if it right **/
    private fun checkAnswer(selectedOption: Int) {
        if (selectedOption == task.correctOption) {
            // Correct answer selected, show feedback and move to the next task
            Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            // Incorrect answer selected, show feedback
            Toast.makeText(context, "Incorrect, try again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: TextQuizTask): TextQuizTaskFragment {
            val fragment = TextQuizTaskFragment()
            val args = Bundle().apply {
                putParcelable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
