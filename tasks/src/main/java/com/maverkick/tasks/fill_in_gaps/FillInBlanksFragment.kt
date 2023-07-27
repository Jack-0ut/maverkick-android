package com.maverkick.tasks.fill_in_gaps

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.maverkick.tasks.R
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.FillInBlanksBinding

class FillInBlanksFragment : Fragment(),TaskActionsListener {

    private var _binding: FillInBlanksBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: FillInBlanks
    private val editTexts = mutableListOf<EditText>() // Make editTexts a class level property

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FillInBlanksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task = arguments?.getParcelable("task") ?: throw IllegalStateException("Task is missing")
        setupTextAndInputs()
    }

    private fun setupTextAndInputs() {
        val parts = task.question.split(Regex("_+"))

        for (i in parts.indices) {
            val part = parts[i]
            if (part.isNotEmpty()) {
                val textView = TextView(context).apply {
                    text = part
                    textSize = 16f
                }
                binding.blanksContainer.addView(textView)
            }

            // Add EditText for each blank, but not after the last part
            if (i < parts.size - 1) {
                val editText = EditText(context).apply {
                    hint = " "
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    val widthFactor = 30
                    val answerLength = task.answer.length
                    width = widthFactor * answerLength

                    // Apply the drawable background
                    setBackgroundResource(R.drawable.fill_in_box)

                    // Add some padding inside the box
                    setPadding(10, 10, 10, 10)
                }
                editTexts.add(editText)
                binding.blanksContainer.addView(editText)
            }
        }
    }

    override fun checkAnswer(): Pair<Boolean, String?> {
        val correctAnswers = mutableListOf<String>()
        for (i in editTexts.indices) {
            val userAnswer = editTexts[i].text.toString().trim()
            val answer = task.answer

            if (userAnswer == answer) {
                editTexts[i].setTextColor(Color.GREEN)
                return Pair(userAnswer == answer ,answer)
            } else {
                editTexts[i].setTextColor(Color.RED)
                correctAnswers.add(answer)
            }
        }
        return Pair(false ,task.answer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: FillInBlanks): FillInBlanksFragment {
            val fragment = FillInBlanksFragment()
            val args = Bundle().apply {
                putParcelable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

