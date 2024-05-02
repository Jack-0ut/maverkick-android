package com.maverkick.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shared_ui.OnItemClickListener
import com.maverkick.common.databinding.ItemLessonDescriptionBinding
import com.maverkick.data.models.Lesson

/**
 * Adapter class to display a list of lessons.
 * Provides an interface for handling clicks on lessons,
 * and methods to manage the underlying data.
 **/
class LessonDescriptionAdapter(private val onLessonClickListener: OnItemClickListener<Lesson>) :
    ListAdapter<Lesson, LessonDescriptionAdapter.LessonViewHolder>(LessonDiffCallback()) {

    // ID of the currently expanded lesson, if any
    private var expandedLessonId: String? = null

    // ViewHolder class to represent a single lesson item
    class LessonViewHolder(private val binding: ItemLessonDescriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.lessonNumber.text = String.format("%02d", lesson.lessonOrder)
            binding.lessonTitle.text = lesson.title
            binding.lessonDuration.text = secondsToMinutesSeconds(lesson.duration)
        }

        private fun secondsToMinutesSeconds(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = LessonViewHolder(binding)

        // Set click listener on the created view holder
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                ?: return@setOnClickListener
            onLessonClickListener.onItemClick(getItem(position))
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.lessonId == newItem.lessonId
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.isContentTheSame(newItem)
        }
    }
}
