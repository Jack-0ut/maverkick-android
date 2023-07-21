package com.example.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.CourseStatistics
import com.example.teacher.databinding.ItemCourseStatsBinding

class CourseStatsAdapter : ListAdapter<CourseStatistics, CourseStatsAdapter.CourseStatsViewHolder>(CourseStatsDiffCallback()) {

    inner class CourseStatsViewHolder(private val binding: ItemCourseStatsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(courseStats: CourseStatistics) {
            binding.courseName.text = courseStats.courseName
            binding.numEnrollments.text = "Enrollments: ${courseStats.numberOfEnrollments}"
            binding.completionRate.text = "Completion Rate: ${courseStats.numberOfCompletions}"
            binding.dropoutRate.text = "Dropouts: ${courseStats.numberOfDropouts}"
            binding.avgRating.text = "Average Rating: ${courseStats.calculateAverageRating()}"
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseStatsViewHolder {
        val binding = ItemCourseStatsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseStatsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseStatsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CourseStatsDiffCallback : DiffUtil.ItemCallback<CourseStatistics>() {
    override fun areItemsTheSame(oldItem: CourseStatistics, newItem: CourseStatistics): Boolean {
        // Here, you should compare item IDs, assuming they are unique.
        // If CourseStatistics doesn't have a unique ID, modify this line accordingly.
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: CourseStatistics, newItem: CourseStatistics): Boolean {
        // Here, you are comparing the full item to check if there are differences.
        // Adjust this if your CourseStatistics class needs a more sophisticated comparison.
        return oldItem == newItem
    }
}