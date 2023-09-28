package com.maverkick.text_lesson.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.common.databinding.ItemLessonDescriptionBinding
import com.maverkick.data.models.TextLesson

class LessonDescriptionAdapter(
    private val lessons: List<TextLesson>
) : RecyclerView.Adapter<LessonDescriptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLessonDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.binding.lessonNumber.text = String.format("%02d", position + 1)
        holder.binding.lessonTitle.text = lesson.title
        holder.binding.lessonDuration.text = secondsToMinutesSeconds(lesson.duration)
    }

    override fun getItemCount(): Int {
        return lessons.size
    }

    inner class ViewHolder(val binding: ItemLessonDescriptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun secondsToMinutesSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
