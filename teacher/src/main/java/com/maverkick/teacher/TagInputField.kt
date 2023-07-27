package com.maverkick.teacher

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

    val tagInputEditText: AutoCompleteTextView
    val chipGroup: ChipGroup

    var onTagAdded: ((String) -> Unit)? = null
    var onTagRemoved: ((String) -> Unit)? = null

    init {
        orientation = VERTICAL

        chipGroup = ChipGroup(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            isSingleLine = false
        }

        addView(chipGroup)

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
                if (s?.endsWith(" ") == true) {
                    val tag = s.trim().toString()
                    if (tag.isNotEmpty()) {
                        onTagAdded?.invoke(tag)
                        tagInputEditText.text = null
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun createChip(tag: String): Chip {
        return Chip(context).apply {
            text = tag
            isCloseIconVisible = true
            setChipBackgroundColorResource(com.maverkick.common.R.color.secondary_color)
            setTextColor(Color.WHITE)
            setCloseIconTintResource(com.maverkick.common.R.color.main_accent_color)
            setOnCloseIconClickListener {
                // When a chip is removed, we remove it from the UI and notify about removal
                chipGroup.removeView(this)
                onTagRemoved?.invoke(tag)
            }
        }
    }


    fun setTags(tags: List<String>) {
        chipGroup.removeAllViews()
        tags.forEach { tag ->
            chipGroup.addView(createChip(tag))
        }
    }

    fun getCurrentTags(): List<String> {
        val tags = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            chip?.let { tags.add(it.text.toString()) }
        }
        return tags
    }
}
