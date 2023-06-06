package com.example.teacher

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Custom Tag Input Field that give user an ability to enter any text and
 * on the space click it's formatted as a Chip element
 **/
class TagInputField(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var maxChips: Int = 5  // Default value
    private var currentChipsCount: Int = 0

    val tagInputEditText: AutoCompleteTextView
    val chipGroup: ChipGroup

    // On max chip number achieved listener
    var onMaxChipsReached: (() -> Unit)? = null

    init {
        orientation = VERTICAL

        // Create a ChipGroup
        chipGroup = ChipGroup(context).apply {
            // Update layout parameters
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            isSingleLine = false
        }

        addView(chipGroup)

        // Create an AutoCompleteTextView
        tagInputEditText = AutoCompleteTextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            hint = "Add a tag"
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.BLACK)
            textSize = 16f
            setPadding(8, 8, 8, 8)
        }
        addView(tagInputEditText)

        tagInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (currentChipsCount < maxChips && s?.endsWith(" ") == true) {
                    val chip = Chip(context).apply {
                        text = s.trim()
                        isCloseIconVisible = true
                        setChipBackgroundColorResource(com.example.common.R.color.secondary_color)
                        setTextColor(Color.WHITE)
                        setCloseIconTintResource(com.example.common.R.color.main_accent_color)
                        setOnCloseIconClickListener {
                            // Decrease chip count when chip is removed
                            chipGroup.removeView(this)
                            currentChipsCount--
                        }
                    }
                    chipGroup.addView(chip)
                    tagInputEditText.text = null
                    // Increase chip count when chip is added
                    currentChipsCount++

                    // Check if maxChips is reached
                    if (currentChipsCount >= maxChips) {
                        onMaxChipsReached?.invoke()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    /** Change the number of chips user could enter */
    fun setMaxChips(count: Int) {
        maxChips = count
    }
}
