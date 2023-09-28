package com.maverkick.tasks.types.fill_in_gaps

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.maverkick.tasks.R
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.FillInBlanksBinding

class FillInGapsFragment : Fragment(),TaskActionsListener {
    private var _binding: FillInBlanksBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: FillInGaps
    private val spinners = mutableListOf<AutoCompleteTextView>()

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
        val patterns = listOf("<GAP\\d+>", "__\\d+__", "__+")

        val parts = splitTextByPatterns(task.text, patterns)

        for (i in parts.indices) {
            val part = parts[i]
            if (part.isNotEmpty()) {
                val textView = TextView(context).apply {
                    text = part
                    textSize = 20f
                    layoutParams = FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                binding.blanksContainer.addView(textView)
            }

            // Add the custom layout for each blank, but not after the last part
            if (i < parts.size - 1) {
                val gapLayout = LayoutInflater.from(context).inflate(R.layout.gap_dropdown_item, binding.blanksContainer, false)
                val autoCompleteTextView = gapLayout.findViewById<AutoCompleteTextView>(R.id.gap_autocomplete)

                autoCompleteTextView.setAdapter(ArrayAdapter(context!!, R.layout.custom_spinner_item, task.gaps[i].options))
                autoCompleteTextView.inputType = InputType.TYPE_NULL

                binding.blanksContainer.addView(gapLayout)
                spinners.add(autoCompleteTextView)
            }
        }
    }

    override fun checkAnswer(onResult: (Pair<Boolean, String?>) -> Unit) {
        val correctAnswers = task.gaps.map { it.answer }
        val userAnswers = spinners.map { it.text.toString() } // Changed this line to retrieve the text input
        val isCorrect = correctAnswers.zip(userAnswers).all { (answer, userAnswer) -> userAnswer == answer }

        // Return the result through the provided callback
        onResult(Pair(isCorrect, if (isCorrect) "Yeah, you've done that!" else "Keep trying! Just more efforts!"))
    }

    private fun splitTextByPatterns(text: String, patterns: List<String>): List<String> {
        var result = listOf(text)
        for (pattern in patterns) {
            result = result.flatMap { it.split(Regex(pattern)) }
        }
        return result
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: FillInGaps): FillInGapsFragment {
            val fragment = FillInGapsFragment()
            val args = Bundle().apply {
                putParcelable("task", task)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

