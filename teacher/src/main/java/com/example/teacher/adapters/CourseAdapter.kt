package com.example.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.data.models.Course
import com.example.teacher.databinding.TeacherItemCourseBinding

/**
 * Adapter for displaying the picture of the course, title and the edit button
 * in the Home Teacher, basically listing all of the course for a Teacher
 * @param clickListener the action that happens on the course click
 **/
class CourseAdapter(private val clickListener: OnCourseClickListener) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    interface OnCourseClickListener {
        fun onCourseClick(courseId: String)
    }

    inner class CourseViewHolder(private val binding: TeacherItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.courseTitle.text = course.courseName

            // Loading the image from the URL
            Glide.with(binding.root.context)
                .load(course.poster)
                .into(binding.courseImage)

            // open the particular course for editing
            binding.root.setOnClickListener {
                clickListener.onCourseClick(course.courseId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = TeacherItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        // Here, you should compare item IDs, assuming they are unique.
        // If Course doesn't have a unique ID, modify this line accordingly.
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        // Here, you are comparing the full item to check if there are differences.
        // Adjust this if your Course class needs a more sophisticated comparison.
        return oldItem == newItem
    }
}
