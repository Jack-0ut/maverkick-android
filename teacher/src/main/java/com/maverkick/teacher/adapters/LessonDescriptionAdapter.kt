package com.maverkick.teacher.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.common.databinding.ItemLessonDescriptionBinding
import com.maverkick.data.models.VideoLesson
import com.maverkick.teacher.videoplayer.VideoPlayerActivity

/**
 * Adapter class to display a list of video lessons.
 * Provides an interface for handling clicks on lessons,
 * and methods to manage the underlying data.
 **/
class LessonDescriptionAdapter : ListAdapter<VideoLesson, LessonDescriptionAdapter.LessonViewHolder>(LessonDiffCallback()) {

    // ID of the currently expanded lesson, if any
    private var expandedLessonId: String? = null

    // Callback interface for handling clicks on lessons
    private var onLessonClickListener: OnLessonClickListener? = null

    // Interface declaration for a callback to be invoked when a lesson is clicked
    interface OnLessonClickListener {
        fun onLessonClick(lessonId: String, position: Int)
    }

    // Setter method to register the callback
    fun setOnLessonClickListener(listener: OnLessonClickListener) {
        this.onLessonClickListener = listener
    }

    // ViewHolder class to represent a single video lesson item
    inner class LessonViewHolder(private val binding: ItemLessonDescriptionBinding) : RecyclerView.ViewHolder(binding.root) {

        // Initialize block to set the click listener for the entire view item
        init {
            itemView.setOnClickListener {
                val videoLesson = getItem(adapterPosition)
                // Construct an intent to open VideoPlayerActivity with the selected lesson's video URL
                val intent = Intent(it.context, VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, videoLesson.videoUrl)
                }
                it.context.startActivity(intent) // Start the activity
            }
        }

        // Bind the views in the ViewHolder to the data
        fun bind(videoLesson: VideoLesson) {
            binding.lessonNumber.text = String.format("%02d", videoLesson.lessonOrder)
            binding.lessonTitle.text = videoLesson.title
            binding.lessonDuration.text = secondsToMinutesSeconds(videoLesson.duration)
        }

        // Convert seconds to a minutes:seconds format string
        private fun secondsToMinutesSeconds(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    // Create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    // Bind data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Handler for when a lesson is clicked
    fun onLessonClick(lessonId: String, position: Int) {
        expandedLessonId = if (expandedLessonId == lessonId) null else lessonId
        notifyItemChanged(position) // Notify that the item has been changed
    }
}

// DiffUtil callback to calculate the difference between two non-null items in a list
class LessonDiffCallback : DiffUtil.ItemCallback<VideoLesson>() {
    override fun areItemsTheSame(oldItem: VideoLesson, newItem: VideoLesson): Boolean {
        return oldItem.lessonId == newItem.lessonId // Return true if the items represent the same object
    }

    override fun areContentsTheSame(oldItem: VideoLesson, newItem: VideoLesson): Boolean {
        return oldItem == newItem // Return true if the items have the same data
    }
}