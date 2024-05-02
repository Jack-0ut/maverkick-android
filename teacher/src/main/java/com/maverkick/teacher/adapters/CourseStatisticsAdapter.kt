package com.maverkick.teacher.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.data.models.CourseStatistics
import com.maverkick.data.models.StatisticType
import com.maverkick.teacher.databinding.*

/** Display course name + StatisticsItemAdapter for every course **/
class CourseStatisticsAdapter :
    RecyclerView.Adapter<CourseStatisticsAdapter.CourseStatisticsViewHolder>() {

    private var courseList: List<CourseStatistics> = listOf()

    fun submitList(list: List<CourseStatistics>) {
        courseList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseStatisticsViewHolder {
        val binding = ItemStatisticsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseStatisticsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseStatisticsViewHolder, position: Int) {
        val courseStatistic = courseList[position]
        holder.bind(courseStatistic)
    }

    override fun getItemCount(): Int = courseList.size

    inner class CourseStatisticsViewHolder(private val binding: ItemStatisticsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stats: CourseStatistics) {
            val statisticsItemAdapter = StatisticsItemAdapter()
            binding.statisticsRecyclerView.adapter = statisticsItemAdapter
            statisticsItemAdapter.submitList(listOf(stats))
        }
    }
}

/** Display the statistics for the particular course **/
class StatisticsItemAdapter : ListAdapter<CourseStatistics, RecyclerView.ViewHolder>(CourseStatisticsDiffCallback()) {

    private val items = StatisticType.values()

    override fun getItemCount(): Int = currentList.size * items.size

    override fun getItemViewType(position: Int): Int {
        return items[position % items.size].ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (StatisticType.values()[viewType]) {
            StatisticType.ENROLLMENTS -> EnrollmentsViewHolder(ItemEnrollmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            StatisticType.COMPLETION_RATE -> CompletionRateViewHolder(ItemCompletionRateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            StatisticType.DROPOUTS -> DropoutViewHolder(ItemDropoutsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            StatisticType.AVERAGE_RATING -> AverageRatingViewHolder(ItemAvgRatingsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val course = getItem(position / items.size)
        when (items[position % items.size]) {
            StatisticType.ENROLLMENTS -> (holder as EnrollmentsViewHolder).bind(course)
            StatisticType.COMPLETION_RATE -> (holder as CompletionRateViewHolder).bind(course)
            StatisticType.DROPOUTS -> (holder as DropoutViewHolder).bind(course)
            StatisticType.AVERAGE_RATING -> (holder as AverageRatingViewHolder).bind(course)
        }
    }
}

class EnrollmentsViewHolder(private val binding: ItemEnrollmentsBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(stats: CourseStatistics) {
        binding.tvEnrollmentCount.text = stats.numberOfEnrollments.toString()
    }
}

class CompletionRateViewHolder(private val binding: ItemCompletionRateBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(stats: CourseStatistics) {
        val completionRate = if (stats.numberOfEnrollments > 0) {
            (stats.numberOfCompletions.toFloat() / stats.numberOfEnrollments.toFloat()) * 100
        } else {
            0f
        }
        binding.tvCompletionRate.text = "${completionRate.toInt()}%"
        binding.progressCompletionRate.setProgressCompat(completionRate.toInt(), true)
    }
}

class DropoutViewHolder(private val binding: ItemDropoutsBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(stats: CourseStatistics) {
        val dropoutRate = if (stats.numberOfEnrollments > 0) {
            (stats.numberOfDropouts.toFloat() / stats.numberOfEnrollments.toFloat()) * 100
        } else {
            0f
        }

        binding.tvDropoutRate.text = "${dropoutRate.toInt()}%"
        binding.progressDropoutRate.setProgressCompat(dropoutRate.toInt(), true)
    }
}

class AverageRatingViewHolder(private val binding: ItemAvgRatingsBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(stats: CourseStatistics) {
        val averageRating = if (stats.totalNumberOfRatings > 0) {
            stats.sumOfRatings.toFloat() / stats.totalNumberOfRatings.toFloat()
        } else {
            0f
        }
       binding.tvAvgRating.text = String.format("%.1f", averageRating)
    }
}

private class CourseStatisticsDiffCallback : DiffUtil.ItemCallback<CourseStatistics>() {
    override fun areItemsTheSame(oldItem: CourseStatistics, newItem: CourseStatistics): Boolean {
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(oldItem: CourseStatistics, newItem: CourseStatistics): Boolean {
        return oldItem == newItem
    }
}

