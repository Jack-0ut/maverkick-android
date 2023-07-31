package com.maverkick.tasks.matching

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.MatchingBinding

class MatchingFragment : Fragment(), TaskActionsListener {

    private lateinit var task: Matching
    private var _binding: MatchingBinding? = null
    private val binding get() = _binding!!

    private var selectedTerm: String? = null
    private var selectedDefinition: String? = null
    private val matchedPairs = mutableListOf<MatchingPair>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        task = arguments?.getParcelable("task") ?: throw IllegalStateException("Task is missing")



        // set the question and description
        binding.question.text = task.question

        // Randomize and separate the pairs into two lists
        val randomizedPairs = task.pairs.shuffled()
        val terms = randomizedPairs.map { it.term }
        val definitions = randomizedPairs.map { it.definition }

        Log.d("MatchingFragment", "terms size: ${terms.size}, definitions size: ${definitions.size}")
        // Initialize the adapters
        val termsAdapter = TermsAdapter(terms) { term ->
            selectedTerm = term
            checkPair()
        }
        val definitionsAdapter = DefinitionsAdapter(definitions) { definition ->
            selectedDefinition = definition
            checkPair()
        }

        // Set the LayoutManagers for the RecyclerViews
        binding.termsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.definitionsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Set the adapters to the RecyclerViews
        binding.termsRecyclerView.adapter = termsAdapter
        binding.definitionsRecyclerView.adapter = definitionsAdapter
    }

    private fun checkPair() {
        if (selectedTerm != null && selectedDefinition != null) {
            if (task.pairs.contains(MatchingPair(selectedTerm!!, selectedDefinition!!))) {
                // The pair is a match! Do something to indicate this to the user...
                matchedPairs.add(MatchingPair(selectedTerm!!, selectedDefinition!!))
            } else {
                // The pair is not a match. Do something to indicate this to the user...
            }

            // Reset the selected term and definition
            selectedTerm = null
            selectedDefinition = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(task: Matching): MatchingFragment {
            val fragment = MatchingFragment()
            val args = Bundle()
            args.putParcelable("task", task)
            fragment.arguments = args
            return fragment
        }
    }

    override fun checkAnswer(): Pair<Boolean, String?> {
        // Sort the pairs in both lists to ensure they can be compared correctly
        val originalPairs = task.pairs.sortedBy { it.term }
        val userPairs = matchedPairs.sortedBy { it.term }

        return if (originalPairs == userPairs) {
            // The user has correctly matched all pairs
            Pair(true, null)
        } else {
            // The user has not correctly matched all pairs
            Pair(false, "The correct pairs are: $originalPairs")
        }
    }
}
