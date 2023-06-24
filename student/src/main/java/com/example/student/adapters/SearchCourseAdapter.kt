package com.example.student.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.data.models.SearchCourseHit
import com.example.student.databinding.ItemSearchCourseBinding

interface OnSearchCourseClickListener {
    fun onSearchCourseClick(courseId: String)
}

/**
 * Display the course that fits user search query
 **/
class SearchCourseAdapter(private val onSearchCourseClickListener: OnSearchCourseClickListener) :
    ListAdapter<SearchCourseHit, SearchCourseAdapter.SearchCourseViewHolder>(SearchHitDiffCallback()) {

    inner class SearchCourseViewHolder(private val binding: ItemSearchCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    onSearchCourseClickListener.onSearchCourseClick(item.objectId)
                }
            }
        }
        fun bind(searchHit: SearchCourseHit) {
            binding.courseTitle.text = searchHit.courseName

            // Loading the image from the URL
            Glide.with(binding.root.context)
                .load(searchHit.poster)
                .into(binding.courseImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchCourseViewHolder {
        val binding = ItemSearchCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchCourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class SearchHitDiffCallback : DiffUtil.ItemCallback<SearchCourseHit>() {
    override fun areItemsTheSame(oldItem: SearchCourseHit, newItem: SearchCourseHit): Boolean {
        // Here, you should compare item IDs, assuming they are unique.
        return oldItem.objectId == newItem.objectId
    }

    override fun areContentsTheSame(oldItem: SearchCourseHit, newItem: SearchCourseHit): Boolean {
        // Here, you are comparing the full item to check if there are differences.
        // Adjust this if your SearchCourseHit class needs a more sophisticated comparison.
        return oldItem == newItem
    }
}
