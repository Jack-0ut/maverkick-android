package com.maverkick.student.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maverkick.data.models.Course
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.student.databinding.ItemCourseGridBinding

/**
 * Adapter to display courses in a grid format.
 * Takes a list of courses and provides a way to interact with them through clicks.
 *
 * @param onCourseClickListener Listener for handling click events on the course items.
 **/
class CourseGridAdapter(private val onCourseClickListener: (Course) -> Unit) :
    ListAdapter<Course, CourseGridAdapter.CourseViewHolder>(CourseGeneralDiffCallback) {

    inner class CourseViewHolder(private val binding: ItemCourseGridBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                val item = getItem(position)
                onCourseClickListener(item)
            }
        }

        fun bind(course: Course) {
            with(binding) {
                courseTitle.text = course.courseName  // Adjust these based on your actual layout views
                when (course) {
                    is VideoCourse -> {
                        Glide.with(root.context)
                            .load(course.poster)
                            .into(courseImage)
                    }
                    is TextCourse -> {
                        Glide.with(root.context)
                            .load(course.poster)
                            .into(courseImage)
                    }
                    else -> {
                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(Color.BLUE, Color.RED)
                        )
                        courseImage.background = gradientDrawable
                        courseImage.setImageDrawable(null)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object CourseGeneralDiffCallback : DiffUtil.ItemCallback<Course>() {
    override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
        return oldItem == newItem
    }
}
