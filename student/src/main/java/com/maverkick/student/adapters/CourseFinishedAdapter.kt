package com.maverkick.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.data.models.Course
import com.maverkick.student.databinding.ItemCourseFinishedBinding

/**
 * Adapter for displaying the finished courses and their titles in the Profile -> Courses section.
 */
class CourseFinishedAdapter :
    ListAdapter<Course, CourseFinishedAdapter.CourseFinishedViewHolder>(CourseFinishedDiffCallback()) {

    inner class CourseFinishedViewHolder(private val binding: ItemCourseFinishedBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.finishedCourseTitle.text = course.courseName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseFinishedViewHolder {
        val binding = ItemCourseFinishedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseFinishedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseFinishedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CourseFinishedDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
