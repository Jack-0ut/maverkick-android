package com.maverkick.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.common.databinding.ItemLessonDescriptionBinding
import com.maverkick.data.models.VideoLesson

/**
 * Simple adapter for showing the lessons for the given course.
 * It's just number, title and duration
 **/
class CourseLessonAdapter : ListAdapter<VideoLesson, CourseLessonAdapter.CourseLessonViewHolder>(LessonDiffCallback()) {

    inner class CourseLessonViewHolder(private val binding: ItemLessonDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lesson: VideoLesson, number: Int) {
            binding.lessonNumber.text = String.format("%02d", number)
            binding.lessonTitle.text = lesson.title
            binding.lessonDuration.text = secondsToMinutesSeconds(lesson.duration)
        }

        private fun secondsToMinutesSeconds(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    class LessonDiffCallback : DiffUtil.ItemCallback<VideoLesson>() {
        override fun areItemsTheSame(oldItem: VideoLesson, newItem: VideoLesson): Boolean {
            return oldItem.lessonId == newItem.lessonId
        }

        override fun areContentsTheSame(oldItem: VideoLesson, newItem: VideoLesson): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseLessonViewHolder {
        val binding = ItemLessonDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseLessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseLessonViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
}
