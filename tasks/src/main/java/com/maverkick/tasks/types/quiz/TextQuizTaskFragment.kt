package com.maverkick.tasks.types.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.maverkick.tasks.R
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.TextQuizBinding

/**
 * Fragment for the classic text quiz task.
 * This basically takes the quiz, displays it and checks the answer
 **/
class TextQuizTaskFragment : Fragment(), TaskActionsListener {
    private var _binding: TextQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: TextQuiz

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TextQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task = arguments?.getParcelable("task") ?: throw IllegalStateException("Task is missing")

        // set the question and options
        binding.question.text = task.question

        val optionsRadioGroup = binding.options
        optionsRadioGroup.removeAllViews()
        val layoutInflater = LayoutInflater.from(context)

        task.options.forEachIndexed { index, optionText ->
            val radioButton = layoutInflater.inflate(R.layout.radio_button_item, optionsRadioGroup, false) as RadioButton
            radioButton.text = optionText
            radioButton.id = index
            optionsRadioGroup.addView(radioButton)
        }
    }

    /** Method that checks the chosen answer and shows if it right **/
    override fun checkAnswer(onResult: (Pair<Boolean, String?>) -> Unit) {
        val selectedOptionId = binding.options.checkedRadioButtonId
        val selectedOption = binding.options.findViewById<RadioButton>(selectedOptionId)

        val result = if (selectedOption != null) {
            val selectedOptionText = selectedOption.text.toString()

            if (selectedOptionText == task.answer) {
                // Correct answer selected, change color to green
                selectedOption.setTextColor(Color.parseColor("#66BB6A")) // soft green
                Pair(true, "You're a real Quiz Hunter!")
            } else {
                // Incorrect answer selected, change color to red
                selectedOption.setTextColor(Color.parseColor("#EF5350")) // soft red
                Pair(false, "Sorry, but not this time!")
            }
        } else {
            // No option selected
            Pair(false, task.answer)
        }

        // Return result through callback
        onResult(result)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: TextQuiz): TextQuizTaskFragment {
            val fragment = TextQuizTaskFragment()
            val args = Bundle().apply {
                putParcelable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
