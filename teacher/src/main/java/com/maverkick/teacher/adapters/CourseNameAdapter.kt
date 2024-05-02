package com.maverkick.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.data.models.CourseStatistics
import com.maverkick.teacher.databinding.ItemCourseNameBinding

class CourseNameAdapter(private val itemClickListener: (Int) -> Unit) :
    RecyclerView.Adapter<CourseNameAdapter.CourseNameViewHolder>() {

    private var courseList: List<CourseStatistics> = listOf()

    fun submitList(list: List<CourseStatistics>) {
        courseList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseNameViewHolder {
        val binding = ItemCourseNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseNameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseNameViewHolder, position: Int) {
        holder.bind(courseList[position])
    }

    override fun getItemCount(): Int = courseList.size

    inner class CourseNameViewHolder(private val binding: ItemCourseNameBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(courseStatistics: CourseStatistics) {
            binding.root.setOnClickListener {
                itemClickListener(adapterPosition)
            }
            // Set the course name using view binding
            binding.courseName.text = courseStatistics.courseName
        }
    }
}
