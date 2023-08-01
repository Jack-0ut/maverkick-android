package com.maverkick.tasks.matching

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maverkick.tasks.TaskActionsListener
import com.maverkick.tasks.databinding.MatchingBinding

/** The Fragment for the Matching Task**/
class MatchingFragment : Fragment(), TaskActionsListener {
    private lateinit var task: Matching
    private var _binding: MatchingBinding? = null
    private val binding get() = _binding!!

    private var selectedTerm: String? = null
    private var selectedDefinition: String? = null
    private val chosenPairs = mutableListOf<MatchingPair>()
    private val colors = listOf(
        Color.parseColor("#FFB74D"),  // Orange
        Color.parseColor("#BA68C8"),  // Purple
        Color.parseColor("#4DB6AC"),  // Teal
        Color.parseColor("#FF8A65"),  // Deep Orange
        Color.parseColor("#4FC3F7")   // Light Blue
    )
    private var colorIndex = 0

    private lateinit var termsAdapter: TermsAdapter
    private lateinit var definitionsAdapter: DefinitionsAdapter
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
        val terms = randomizedPairs.map { it.term }.shuffled()
        val definitions = randomizedPairs.map { it.definition }

        // Initialize the adapters
        termsAdapter = TermsAdapter(terms) { term ->
            selectedTerm = term
            termsAdapter.colorizeSelectedTerm(colors[colorIndex])
            colorIndex = (colorIndex + 1) % colors.size
            checkPair()
        }
        definitionsAdapter = DefinitionsAdapter(definitions) { definition ->
            selectedDefinition = definition
            definitionsAdapter.colorizeSelectedDefinition(colors[colorIndex])
            colorIndex = (colorIndex + 1) % colors.size
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
            // Add pair to chosen pairs
            chosenPairs.add(MatchingPair(selectedTerm!!, selectedDefinition!!))

            // Colorize the pair
            termsAdapter.colorizeSelectedTerm(colors[colorIndex])
            definitionsAdapter.colorizeSelectedDefinition(colors[colorIndex])

            // Reset the selected term and definition
            selectedTerm = null
            selectedDefinition = null

            // Reset the selected items in the adapters
            termsAdapter.resetSelectedTerm()
            definitionsAdapter.resetSelectedDefinition()

            // Move to the next color
            colorIndex = (colorIndex + 1) % colors.size
        }
    }

    override fun checkAnswer(): Pair<Boolean, String?> {
        // Sort the pairs in both lists to ensure they can be compared correctly
        val originalPairs = task.pairs.sortedBy { it.term }
        val userPairs = chosenPairs.sortedBy { it.term }

        return if (originalPairs == userPairs) {
            // The user has correctly matched all pairs
            Pair(true, null)
        } else {
            // The user has not correctly matched all pairs
            Pair(false, "Hey,not exactly right.")
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
}
