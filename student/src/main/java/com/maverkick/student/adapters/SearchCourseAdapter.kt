package com.maverkick.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maverkick.data.models.VideoCourse
import com.maverkick.student.databinding.ItemSearchCourseBinding


/**
 * Adapter to display the courses that fit a user's search query.
 * Takes a list of video courses and provides a way to interact with them through clicks.
 *
 * @param onCourseClickListener Listener for handling click events on the course items.
 **/
class SearchCourseAdapter(private val onCourseClickListener: (VideoCourse) -> Unit) :
    ListAdapter<VideoCourse, SearchCourseAdapter.CourseViewHolder>(SearchCourseDiffCallback) {

    inner class CourseViewHolder(private val binding: ItemSearchCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                val item = getItem(position)
                onCourseClickListener(item) // Using lambda for concise onClick handling
            }
        }

        fun bind(videoCourse: VideoCourse) {
            with(binding) {
                courseTitle.text = videoCourse.courseName
                Glide.with(root.context)
                    .load(videoCourse.poster)
                    .into(courseImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemSearchCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object SearchCourseDiffCallback : DiffUtil.ItemCallback<VideoCourse>() {
    override fun areItemsTheSame(oldItem: VideoCourse, newItem: VideoCourse): Boolean {
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: VideoCourse, newItem: VideoCourse): Boolean {
        return oldItem == newItem
    }
}
