package com.example.teacher.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.Lesson
import com.example.teacher.databinding.ItemLessonShortBinding
import com.example.teacher.videoplayer.VideoPlayerActivity

/**
 * Adapter for displaying the lesson title and the video length
 * and also toggle icon, click on which will open the video
 **/
class LessonAdapter : ListAdapter<Lesson, LessonAdapter.LessonViewHolder>(LessonDiffCallback()) {
    private var expandedLessonId: String? = null
    private var onLessonClickListener: OnLessonClickListener? = null

    interface OnLessonClickListener {
        fun onLessonClick(lessonId: String, position: Int)
    }

    fun setOnLessonClickListener(listener: OnLessonClickListener) {
        this.onLessonClickListener = listener
    }

    inner class LessonViewHolder(private val binding: ItemLessonShortBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            binding.lessonTitle.text = lesson.title
            binding.lessonLength.text = secondsToMinutesSeconds(lesson.duration)

            // Set the click listener for the toggle button
            binding.toggleDetailsButton.setOnClickListener {
                val intent = Intent(it.context, VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, lesson.videoUrl)
                }
                it.context.startActivity(intent)
            }

        }

        private fun secondsToMinutesSeconds(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonShortBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun onLessonClick(lessonId: String, position: Int) {
        expandedLessonId = if (expandedLessonId == lessonId) null else lessonId
        notifyItemChanged(position)
    }
}


class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
    override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        // Here, you should compare item IDs, assuming they are unique.
        // If Lesson doesn't have a unique ID, modify this line accordingly.
        return oldItem.lessonId == newItem.lessonId
    }

    override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        // Here, you are comparing the full item to check if there are differences.
        // Adjust this if your Lesson class needs a more sophisticated comparison.
        return oldItem == newItem
    }
}
