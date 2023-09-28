package com.maverkick.student.adapters

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shared_ui.OnItemClickListener
import com.maverkick.data.models.Course
import com.maverkick.data.models.CourseType
import com.maverkick.student.databinding.ItemCourseOverviewBinding

/**
 * Adapter class for displaying a list of courses in a RecyclerView.
 */
class CourseOverviewAdapter(private val onCourseClickListener: OnItemClickListener<Course>) :
    ListAdapter<Course, CourseOverviewAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(private val binding: ItemCourseOverviewBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    onCourseClickListener.onItemClick(item) // Using the generic interface here
                }
            }
        }

        fun bind(course: Course) {
            val backgroundColor = getRandomColor()
            val backgroundDrawable = binding.root.background.mutate()
            backgroundDrawable.colorFilter = PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
            binding.root.background = backgroundDrawable

            binding.courseName.text = course.courseName
            // Set text color based on background color luminance
            val textColor = if (isColorDark(backgroundColor)) Color.WHITE else Color.BLACK
            binding.courseName.setTextColor(textColor)

            when (course.type) {
                CourseType.TEXT -> binding.courseTypeIcon.setImageResource(com.maverkick.common.R.drawable.ic_text_course)
                CourseType.VIDEO -> binding.courseTypeIcon.setImageResource(com.maverkick.common.R.drawable.ic_video_course)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseOverviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255
        return darkness >= 0.5
    }
    /**
     * Generates and returns a random color.
     *
     * @return A random color that fits the background.
     */
    private fun getRandomColor(): Int {
        val colors = listOf(
            "#FFA726", "#4DB6AC", "#BA68C8", "#4FC3F7",
            "#FF5252", "#7C4DFF", "#64DD17", "#D4E157"
        )

        return Color.parseColor(colors.shuffled().first())
    }
}
