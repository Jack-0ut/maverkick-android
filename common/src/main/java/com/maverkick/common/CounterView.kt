package com.maverkick.common

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
/**
 * View for getting user's age and time to learn
 **/
class CounterView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    val minusIcon: ImageView
    val plusIcon: ImageView
    val valueTextView: TextView

    var minValue = 0
    var maxValue = 100
    var value = 0
        set(value) {
            field = value
            valueTextView.text = value.toString()
        }

    var stepSize = 1

    var onValueChangeListener: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.counter_view, this, true)

        minusIcon = findViewById(R.id.minus_icon)
        plusIcon = findViewById(R.id.plus_icon)
        valueTextView = findViewById(R.id.value_text_view)

        attrs?.let {
            val attributes = context.obtainStyledAttributes(it, R.styleable.CounterView)
            try {
                minValue = attributes.getInt(R.styleable.CounterView_min_value, 0)
                maxValue = attributes.getInt(R.styleable.CounterView_max_value, 100)
                value = attributes.getInt(R.styleable.CounterView_current_value, 0)
                val iconColor = attributes.getColor(R.styleable.CounterView_icon_color, Color.BLACK)

                minusIcon.setColorFilter(iconColor)
                plusIcon.setColorFilter(iconColor)
                valueTextView.setTextColor(iconColor)

            } finally {
                attributes.recycle()
            }
        }

        minusIcon.setOnClickListener {
            if (value - stepSize >= minValue) {
                value -= stepSize
                onValueChangeListener?.invoke(value)
            }
        }

        plusIcon.setOnClickListener {
            if (value + stepSize <= maxValue) {
                value += stepSize
                onValueChangeListener?.invoke(value)
            }
        }
    }
}
