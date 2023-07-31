package com.maverkick.tasks.open_answer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.OpenAnswerBinding

class OpenAnswerFragment : Fragment(), TaskActionsListener {
    private var _binding: OpenAnswerBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: OpenAnswer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OpenAnswerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task = arguments?.getParcelable("task") as OpenAnswer? ?: throw IllegalStateException("Task is missing")

        // set the question and description
        binding.question.text = task.question
        binding.description.text = task.description
    }
    // TODO here we gonna send the answer to the endpoint to check it's approval rate
    override fun checkAnswer(): Pair<Boolean, String?> {
        val userAnswer = binding.answer.text.toString()

        return if (userAnswer.isNotEmpty()) {
            if (userAnswer == task.answer) {
                // Correct answer entered, change color to green
                binding.answer.setTextColor(Color.GREEN)
                Pair(true, null)
            } else {
                // Incorrect answer entered, change color to red
                binding.answer.setTextColor(Color.RED)
                Pair(false, task.answer)
            }
        } else {
            // No answer entered
            Pair(false, task.answer)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: OpenAnswer): OpenAnswerFragment {
            val fragment = OpenAnswerFragment()
            val args = Bundle().apply {
                putParcelable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
