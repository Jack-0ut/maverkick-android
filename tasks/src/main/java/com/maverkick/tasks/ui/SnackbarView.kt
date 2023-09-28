package com.maverkick.tasks.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.maverkick.tasks.R

class SnackbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val messageTextView: TextView
    private val iconImageView: ImageView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.snackbar_view, this, true)

        messageTextView = view.findViewById(R.id.snackbar_text)
        iconImageView = view.findViewById(R.id.snackbar_icon)
    }

    fun showCorrect(message: String) {
        setBackgroundColor(ContextCompat.getColor(context, com.maverkick.common.R.color.green))
        iconImageView.setImageResource(R.drawable.ic_correct)
        messageTextView.text = message

        visibility = VISIBLE

        postDelayed({ visibility = GONE }, 3000)  // Hide after 3 seconds
    }

    fun showIncorrect(message: String) {
        setBackgroundColor(ContextCompat.getColor(context, com.maverkick.common.R.color.red))
        iconImageView.setImageResource(R.drawable.ic_incorrect)
        messageTextView.text = message

        visibility = VISIBLE

        postDelayed({ visibility = GONE }, 3000)  // Hide after 3 seconds
    }
}
