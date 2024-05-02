package com.maverkick.tasks.types.true_or_false

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.TrueOrFalseBinding

class TrueOrFalseFragment : Fragment(), TaskActionsListener {
    private var _binding: TrueOrFalseBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: TrueOrFalse
    private var userAnswer: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TrueOrFalseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        task = arguments?.getParcelable("task") as TrueOrFalse? ?: throw IllegalStateException("Task is missing")
        binding.apply {
            taskText.text = "Is this statement True or False?"
            questionText.text = task.statement

            trueButton.setAnswer("True")
            falseButton.setAnswer("False")
        }
    }

    private fun View.setAnswer(answer: String) {
        setOnClickListener {
            resetAnswerColors()
            userAnswer = answer
            checkAnswer {
                val color = ContextCompat.getColor(context, com.maverkick.common.R.color.maverkick_light_green)
                setBackgroundColor(color)
            }
        }
    }

    private fun resetAnswerColors() {
        val defaultColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        binding.trueButton.setBackgroundColor(defaultColor)
        binding.falseButton.setBackgroundColor(defaultColor)
    }

    override fun checkAnswer(onResult: (Pair<Boolean, String?>) -> Unit) {
        val isCorrect = userAnswer == task.answer
        Log.d("TrueOrFalseFragment", "Is Correct: $isCorrect")
        val message = if (isCorrect) {
            "Well done! You are correct."
        } else {
            "Oops! No, it's not."
        }
        onResult(Pair(isCorrect, message))
    }

    override fun onOptionSelected(isSelected: Boolean) {
        TODO("Not yet implemented")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: TrueOrFalse): TrueOrFalseFragment {
            val fragment = TrueOrFalseFragment()
            val args = Bundle().apply {
                putParcelable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
