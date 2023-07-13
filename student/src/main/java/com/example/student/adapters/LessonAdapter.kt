package com.example.student.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.Lesson
import com.example.student.R
import com.example.student.databinding.ItemLessonBinding
import kotlin.random.Random


/**
 * Adapter for a single lesson that will be shown to the Student
 * on the home page, the part of the learning path
 * Takes the list of lessons from different disciplines that Student should learn today
 * and display it to the HomeFragment
 **/
class LessonAdapter(
    private val onLessonClickListener: OnLessonClickListener,
    private var currentLessonIndex: Int
) : ListAdapter<Lesson, LessonAdapter.LessonViewHolder>(LessonDiffCallback){

    // Define the view holder
    inner class LessonViewHolder(val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(lesson: Lesson) {
            val minutes = lesson.duration / 60
            val seconds = lesson.duration % 60
            binding.videoLength.text = String.format("%02d:%02d", minutes, seconds)

            val color = generateColor()
            binding.colorSquare.setBackgroundColor(color)

            // Change the icon based on whether the lesson is accessible
            if (adapterPosition <= currentLessonIndex) {
                binding.playIcon.setImageResource(R.drawable.ic_play)
            } else {
                binding.playIcon.setImageResource(R.drawable.ic_lock)
            }

            binding.playIcon.isEnabled = adapterPosition <= currentLessonIndex
        }

        private fun generateColor(): Int {
            val rng = Random(Random.nextInt()) // use lessonId as seed
            // Generate a color
            // To ensure the colors are not too dark or too light, we can limit the RGB values
            val red = rng.nextInt(256 - 100) + 100 // values between 100 and 255
            val green = rng.nextInt(256 - 100) + 100
            val blue = rng.nextInt(256 - 100) + 100
            return Color.rgb(red, green, blue)
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

        // Set the onClickListener for the item view
        holder.itemView.setOnClickListener {
            if (position <= currentLessonIndex) {
                onLessonClickListener.onLessonClick(lesson)
            }
        }
    }

    fun updateCurrentLessonIndex(newIndex: Int) {
        currentLessonIndex = newIndex
        notifyDataSetChanged()
    }


    interface OnLessonClickListener {
        fun onLessonClick(lesson: Lesson)
    }

    object LessonDiffCallback : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.lessonId == newItem.lessonId
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem == newItem
        }
    }
}
