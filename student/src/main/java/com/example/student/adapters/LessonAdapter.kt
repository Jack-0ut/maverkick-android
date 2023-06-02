package com.example.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.Lesson
import com.example.student.databinding.ItemLessonBinding


/**
 * Adapter for a single lesson that will be shown to the Student
 * on the home page, the part of the learning path
 * Takes the list of lessons from different disciplines that Student should learn today
 * and display it to the HomeFragment
 **/
class LessonAdapter : ListAdapter<Lesson, LessonAdapter.LessonViewHolder>(LessonDiffCallback()) {

    // Define the view holder
    inner class LessonViewHolder(private val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lesson: Lesson) {
            binding.videoLength.text = lesson.length.toString()
            // TODO add the path to the url of the image of lesson/course
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        // Inflate the item layout with ViewBinding
        val binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        // Get the data model based on position
        val lesson = getItem(position)

        // Bind the data model to the item views
        holder.bind(lesson)
    }
}

class LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
    override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        // Return true if the items have the same ID.
        // This assumes that lessons have unique IDs.
        return oldItem.lessonId == newItem.lessonId
    }

    override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
        // Return true if the items contain the same data.
        // This can be a simple equality check or a more complex comparison,
        // depending on the structure of your data.
        return oldItem == newItem
    }
}
