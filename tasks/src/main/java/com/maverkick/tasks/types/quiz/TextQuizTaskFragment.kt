package com.maverkick.tasks.types.quiz

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.maverkick.tasks.OptionSelectionListener
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

            // Set custom background with the border
            radioButton.setBackgroundResource(R.drawable.radio_button_selector)
            optionsRadioGroup.addView(radioButton)
        }

        // Listen for RadioButton selection changes
        optionsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            notifyOptionSelected(checkedId != -1)
        }

    }

    private fun notifyOptionSelected(isSelected: Boolean) {
        // Check if the parent activity implements OptionSelectionListener and call its method
        (activity as? OptionSelectionListener)?.onOptionSelected(isSelected)
    }

    /** Method that checks the chosen answer and shows if it's right **/
    override fun checkAnswer(onResult: (Pair<Boolean, String?>) -> Unit) {
        val selectedOptionId = binding.options.checkedRadioButtonId
        val selectedOption = binding.options.findViewById<RadioButton>(selectedOptionId)

        if (selectedOption == null) {
            // You can't check the answer if you haven't chosen an option.
            onResult(Pair(false, "Please select an option first!"))
            return
        }

        val selectedOptionText = selectedOption.text.toString()
        if (selectedOptionText == task.answer) {
            // Correct answer selected
            selectedOption.background = createDrawable("#A4DA65", "#A9A9A9")
            selectedOption.setTextColor(Color.parseColor("#FDFCFC"))
            onResult(Pair(true, "You're a real Quiz Hunter!"))
        } else {
            // Incorrect answer selected
            selectedOption.background = createDrawable("#F8675D", "#A9A9A9")
            selectedOption.setTextColor(Color.parseColor("#FDFCFC"))

            // Highlight the correct answer in green
            val correctRadioButton = getRadioButtonByText(task.answer)
            correctRadioButton?.background = createDrawable("#A4DA65", "#A9A9A9")
            correctRadioButton?.setTextColor(Color.parseColor("#FDFCFC"))

            onResult(Pair(false, "Think again!"))
        }

        for (i in 0 until binding.options.childCount) {
            val child = binding.options.getChildAt(i)
            child.isEnabled = false // Disable the child view (RadioButton)
        }
    }

    override fun onOptionSelected(isSelected: Boolean) {}

    private fun createDrawable(backgroundColor: String, strokeColor: String): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = 4f.dpToPx()
        drawable.setColor(Color.parseColor(backgroundColor))
        drawable.setStroke(2, Color.parseColor(strokeColor))
        return drawable
    }

    private fun getRadioButtonByText(text: String): RadioButton? {
        for (i in 0 until binding.options.childCount) {
            val child = binding.options.getChildAt(i)
            if (child is RadioButton && child.text.toString() == text) {
                return child
            }
        }
        return null
    }

    private fun Float.dpToPx(): Float {
        val metrics = Resources.getSystem().displayMetrics
        return this * (metrics.densityDpi / 160f)
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
