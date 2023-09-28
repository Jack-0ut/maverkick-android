package com.maverkick.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.models.VideoCourse
import com.maverkick.teacher.databinding.ItemTeacherCourseBinding

/**
 * Adapter for displaying the picture of the course, title, and the edit button
 * in the Home Teacher, basically listing all of the courses for a Teacher
 * @param clickListener the action that happens on the course click
 **/
class CourseAdapter(private val clickListener: OnItemClickListener<VideoCourse>) : ListAdapter<VideoCourse, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemTeacherCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(videoCourse: VideoCourse) {
            binding.courseTitle.text = videoCourse.courseName

            // Loading the image from the URL
            Glide.with(binding.root.context)
                .load(videoCourse.poster)
                .into(binding.courseImage)

            // Open the particular course for editing
            binding.root.setOnClickListener {
                clickListener.onItemClick(videoCourse) // Using the generic interface here
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemTeacherCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CourseDiffCallback : DiffUtil.ItemCallback<VideoCourse>() {
    override fun areItemsTheSame(oldItem: VideoCourse, newItem: VideoCourse): Boolean {
        return oldItem.courseId == newItem.courseId
    }
    override fun areContentsTheSame(oldItem: VideoCourse, newItem: VideoCourse): Boolean {
        return oldItem == newItem
    }
}
