package com.maverkick.tasks.matching

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.tasks.databinding.MatchingItemDefinitionBinding
import com.maverkick.tasks.databinding.MatchingItemTermBinding

class TermsAdapter(private val terms: List<String>, private val onTermClicked: (String) -> Unit) :
    RecyclerView.Adapter<TermsAdapter.TermViewHolder>() {

    inner class TermViewHolder(private val binding: MatchingItemTermBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(term: String) {
            binding.termText.text = term
            // add a click listener to each item
            binding.root.setOnClickListener {
                onTermClicked(term)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolder {
        val binding = MatchingItemTermBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TermViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TermViewHolder, position: Int) {
        holder.bind(terms[position])
    }

    override fun getItemCount() = terms.size
}

class DefinitionsAdapter(private val definitions: List<String>, private val onDefinitionClicked: (String) -> Unit) :
    RecyclerView.Adapter<DefinitionsAdapter.DefinitionViewHolder>() {

    inner class DefinitionViewHolder(private val binding: MatchingItemDefinitionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(definition: String) {
            binding.definitionText.text = definition
            // add a click listener to each item
            binding.root.setOnClickListener {
                onDefinitionClicked(definition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        val binding = MatchingItemDefinitionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DefinitionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        holder.bind(definitions[position])
    }

    override fun getItemCount() = definitions.size
}
