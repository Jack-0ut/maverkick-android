package com.maverkick.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.models.Course
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.teacher.databinding.ItemTeacherCourseBinding

/**
 * Adapter for displaying the picture of the course, title, and the edit button
 * in the Home Teacher, basically listing all of the courses for a Teacher
 * @param clickListener the action that happens on the course click
 **/
class CourseAdapter(private val clickListener: OnItemClickListener<Course>) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemTeacherCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.courseTitle.text = course.courseName

            // Handle the 'poster' property specific to VideoCourse and TextCourse
            when (course) {
                is VideoCourse -> loadPoster(course.poster)
                is TextCourse -> loadPoster(course.poster)
                else -> {
                    // Handle other types of courses that don't have a 'poster' property
                }
            }

            // Open the particular course for editing
            binding.root.setOnClickListener {
                clickListener.onItemClick(course)
            }
        }

        private fun loadPoster(posterUrl: String?) {
            // Loading the image from the URL
            Glide.with(binding.root.context)
                .load(posterUrl)
                .into(binding.courseImage)
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

class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem.courseId == newItem.courseId
    }
    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
