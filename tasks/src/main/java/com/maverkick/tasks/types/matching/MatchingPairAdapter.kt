package com.maverkick.tasks.types.matching

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maverkick.tasks.databinding.MatchingItemBinding

class TermsAdapter(private val terms: List<String>, private val itemClick: (String) -> Unit) : RecyclerView.Adapter<TermsAdapter.ViewHolder>() {
    var selectedTerm: String? = null
    var termColors = HashMap<String, Int>()

    inner class ViewHolder(val binding: MatchingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val oldSelectedTerm = selectedTerm
                selectedTerm = terms[adapterPosition]
                itemClick(terms[adapterPosition])

                // Refresh the visual state of the old and new selected term
                oldSelectedTerm?.let { notifyItemChanged(terms.indexOf(it)) }
                notifyItemChanged(adapterPosition)
            }
        }

        fun bind(term: String) {
            binding.matchingText.text = term

            // Update the view's selected state and color
            itemView.isSelected = selectedTerm == term
            if (itemView.isSelected) {
                colorizeView(itemView, Color.WHITE)
            } else {
                colorizeView(itemView, termColors[term] ?: Color.WHITE)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MatchingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(terms[position])
    }

    override fun getItemCount() = terms.size

    fun resetSelectedTerm() {
        // Reset the selected term
        val oldSelectedTerm = selectedTerm
        selectedTerm = null
        // Refresh the visual state of the old selected term
        oldSelectedTerm?.let { notifyItemChanged(terms.indexOf(it)) }
    }

    fun colorizeSelectedTerm(color: Int) {
        if (selectedTerm != null) {
            termColors[selectedTerm!!] = color
            notifyItemChanged(terms.indexOf(selectedTerm!!))
        }
    }

    private fun colorizeView(view: View, color: Int) {
        val drawable = view.background as? GradientDrawable
        drawable?.setColor(color)
    }
}

class DefinitionsAdapter(private val definitions: List<String>, private val onDefinitionClicked: (String) -> Unit) :
    RecyclerView.Adapter<DefinitionsAdapter.DefinitionViewHolder>() {

    var selectedDefinition: String? = null
    var definitionColors = HashMap<String, Int>()

    inner class DefinitionViewHolder(private val binding: MatchingItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(definition: String) {
            binding.matchingText.text = definition

            // Update the view's selected state and color
            itemView.isSelected = selectedDefinition == definition
            if (itemView.isSelected) {
                colorizeView(itemView, Color.WHITE)
            } else {
                colorizeView(itemView, definitionColors[definition] ?: Color.WHITE)
            }

            // Add a click listener to each item
            binding.root.setOnClickListener {
                selectedDefinition = definition
                onDefinitionClicked(definition)
                notifyDataSetChanged()
            }
        }
    }

    private fun colorizeView(view: View, color: Int) {
        val drawable = view.background as? GradientDrawable
        drawable?.setColor(color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        val binding = MatchingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DefinitionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        holder.bind(definitions[position])
    }

    override fun getItemCount() = definitions.size

    fun resetSelectedDefinition() {
        // Reset the selected definition
        val oldSelectedDefinition = selectedDefinition
        selectedDefinition = null
        // Refresh the visual state of the old selected definition
        oldSelectedDefinition?.let { notifyItemChanged(definitions.indexOf(it)) }
    }

    fun colorizeSelectedDefinition(color: Int) {
        if (selectedDefinition != null) {
            definitionColors[selectedDefinition!!] = color
            notifyItemChanged(definitions.indexOf(selectedDefinition!!))
        }
    }
}
