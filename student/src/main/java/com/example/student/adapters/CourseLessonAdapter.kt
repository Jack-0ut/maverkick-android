package com.example.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.Lesson
import com.example.student.R
import com.example.student.databinding.ItemCourseLessonBinding

/**
 * Simple adapter for showing the lessons for the given course.
 * It's just number, title and duration
 **/
class CourseLessonAdapter : ListAdapter<Lesson, CourseLessonAdapter.CourseLessonViewHolder>(LessonDiffCallback()) {

    inner class CourseLessonViewHolder(private val binding: ItemCourseLessonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lesson: Lesson, number: Int) {
            binding.lessonNumber.text = binding.root.context.getString(R.string.lesson_number_format, number)
            binding.lessonTitle.text = lesson.title
            binding.lessonDuration.text = secondsToMinutesSeconds(lesson.duration)
        }

        private fun secondsToMinutesSeconds(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.lessonId == newItem.lessonId
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseLessonViewHolder {
        val binding = ItemCourseLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseLessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseLessonViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }
}
