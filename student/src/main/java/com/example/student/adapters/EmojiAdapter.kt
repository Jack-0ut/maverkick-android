package com.example.student.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.student.R

/**
 * Adapter used for the lesson quality evaluation with emoji.
 * Plus added small animation when choosing the particular emoji.
 **/
class EmojiAdapter(
    context: Context,
    resource: Int,
    emojis: List<String>,
    private val itemClickListener: (String) -> Unit
) : ArrayAdapter<String>(context, resource, emojis) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_emoji, parent, false)
        val emojiTextView = view.findViewById<TextView>(R.id.tv_emoji)
        val emoji = getItem(position) as String
        emojiTextView.text = emoji
        emojiTextView.setOnClickListener {
            it.startAnimation(getBounceAnimation())
            it.postDelayed({
                itemClickListener.invoke(emoji)
            }, getBounceAnimation().duration)
        }
        return view
    }

    private fun getBounceAnimation(): AnimationSet {
        val growAnim = ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
        growAnim.duration = 150
        val shrinkAnim = ScaleAnimation(1.5f, 1.0f, 1.5f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
        shrinkAnim.duration = 150
        shrinkAnim.startOffset = 150
        val set = AnimationSet(false)
        set.addAnimation(growAnim)
        set.addAnimation(shrinkAnim)
        return set
    }
}
