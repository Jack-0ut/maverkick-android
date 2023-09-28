package com.maverkick.text_lesson.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.text_lesson.databinding.ItemParagraphBinding

class ParagraphAdapter(private val paragraphs: List<String>) : RecyclerView.Adapter<ParagraphAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParagraphBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(paragraphs[position])
    }

    override fun getItemCount(): Int = paragraphs.size

    inner class ViewHolder(private val binding: ItemParagraphBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(paragraph: String) {
            binding.paragraphText.text = paragraph
        }
    }
}
