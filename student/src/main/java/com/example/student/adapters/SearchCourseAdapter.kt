package com.example.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.data.models.Course
import com.example.student.databinding.ItemSearchCourseBinding

interface OnSearchCourseClickListener {
    fun onSearchCourseClick(courseId: String)
}

/**
 * Display the course that fits user search query
 **/
class SearchCourseAdapter(private val onCourseClickListener: OnSearchCourseClickListener) :
    ListAdapter<Course, SearchCourseAdapter.CourseViewHolder>(SearchCourseDiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemSearchCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    onCourseClickListener.onSearchCourseClick(item.courseId)
                }
            }
        }

        fun bind(course: Course) {
            binding.courseTitle.text = course.courseName

            // Loading the image from the URL
            Glide.with(binding.root.context)
                .load(course.poster)
                .into(binding.courseImage)
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


class SearchCourseDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
