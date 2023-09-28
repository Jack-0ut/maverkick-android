package com.maverkick.student.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shared_ui.OnItemClickListener
import com.maverkick.common.databinding.ItemTextLessonBinding
import com.maverkick.common.databinding.ItemVideoLessonBinding
import com.maverkick.data.models.Lesson
import com.maverkick.data.models.TextLesson
import com.maverkick.data.models.VideoLesson
import com.maverkick.student.R
import kotlin.random.Random

/**
 * Adapter for managing and displaying lessons on the home page.
 * Supports both video and text lessons and handles user interaction like play and read.
 *
 * @param onLessonClickListener The callback to invoke when a lesson item is clicked.
 * @param currentLessonIndex The index of the current lesson the student is on.
 */
class LessonAdapter(
    private val onLessonClickListener: OnItemClickListener<Lesson>,
    private var currentLessonIndex: Int
) : ListAdapter<Lesson, RecyclerView.ViewHolder>(LessonDiffCallback) {

    companion object {
        const val VIEW_TYPE_VIDEO = 1
        const val VIEW_TYPE_TEXT = 2
    }

    inner class VideoLessonViewHolder(val binding: ItemVideoLessonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(videoLesson: VideoLesson) {
            val minutes = videoLesson.duration / 60
            val seconds = videoLesson.duration % 60
            binding.videoLength.text = String.format("%02d:%02d", minutes, seconds)

            val color = generateColor()
            binding.colorSquare.setBackgroundColor(color)

            if (adapterPosition <= currentLessonIndex) {
                binding.playIcon.setImageResource(com.maverkick.common.R.drawable.ic_play)
            } else {
                binding.playIcon.setImageResource(R.drawable.ic_lock)
            }

            binding.playIcon.isEnabled = adapterPosition <= currentLessonIndex
        }

    }

    inner class TextLessonViewHolder(val binding: ItemTextLessonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(textLesson: TextLesson) {
            val minutes = textLesson.duration / 60
            val seconds = textLesson.duration % 60
            binding.textLength.text = String.format("%02d:%02d", minutes, seconds)

            val color = generateColor()
            binding.colorSquare.setBackgroundColor(color)

            if (adapterPosition <= currentLessonIndex) {
                binding.readIcon.setImageResource(com.maverkick.common.R.drawable.ic_read)
            } else {
                binding.readIcon.setImageResource(R.drawable.ic_lock)
            }

            binding.readIcon.isEnabled = adapterPosition <= currentLessonIndex
        }
    }

    private fun generateColor(): Int {
        val rng = Random(Random.nextInt()) // use lessonId as seed
        val red = rng.nextInt(256 - 100) + 100
        val green = rng.nextInt(256 - 100) + 100
        val blue = rng.nextInt(256 - 100) + 100
        return Color.rgb(red, green, blue)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is VideoLesson -> VIEW_TYPE_VIDEO
            is TextLesson -> VIEW_TYPE_TEXT
            else -> throw IllegalArgumentException("Unsupported lesson type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VIDEO -> {
                val binding = ItemVideoLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoLessonViewHolder(binding)
            }
            VIEW_TYPE_TEXT -> {
                val binding = ItemTextLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextLessonViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unsupported view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val lesson = getItem(position)
        when (holder) {
            is VideoLessonViewHolder -> holder.bind(lesson as VideoLesson)
            is TextLessonViewHolder -> holder.bind(lesson as TextLesson)
        }
        holder.itemView.setOnClickListener {
            if (position <= currentLessonIndex) {
                onLessonClickListener.onItemClick(lesson)
            }
        }
    }

    fun updateCurrentLessonIndex(newIndex: Int) {
        currentLessonIndex = newIndex
        notifyDataSetChanged()
    }

    object LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.lessonId == newItem.lessonId
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.isContentTheSame(newItem)
        }
    }
}
