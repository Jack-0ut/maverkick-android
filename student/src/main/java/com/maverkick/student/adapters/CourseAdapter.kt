package com.maverkick.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.models.Course
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.student.databinding.ItemCourseBinding

/**
 * Adapter for displaying the courses and their titles in the Profile -> Courses section.
 * Users can also un-enroll from the course by clicking on it.
 *
 * @param interactionListener An interface that handles the interaction with course items.
 */
class CourseAdapter(private val interactionListener: OnItemClickListener<Course>) :
    ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.leaveButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    interactionListener.onItemClick(item)
                }
            }
        }

        fun bind(course: Course) {
            binding.courseTitle.text = course.courseName
            when (course) {
                is VideoCourse -> binding.icon.setImageResource(com.maverkick.common.R.drawable.ic_video_course)
                is TextCourse -> binding.icon.setImageResource(com.maverkick.common.R.drawable.ic_text_course   )
            }
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
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
