package com.example.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.Course
import com.example.student.databinding.ItemCourseBinding

/**
 * Adapter for displaying the picture of the course
 * and it's title in the Profile -> Courses
 * @param courseList stores the list of Courses, on which Student currently enrolled
 **/
class CourseAdapter : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.courseTitle.text = course.courseName
            // TODO set image source for the course
            // binding.courseThumbnail.setImageResource(...)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
