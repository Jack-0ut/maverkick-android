package com.maverkick.student.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.maverkick.student.databinding.CourseFinishedNotificationBinding

/**
 * A fragment that displays a notification taking up half the screen height.
 */
class HalfScreenNotificationFragment : DialogFragment() {

    // Declare binding variable
    private var _binding: CourseFinishedNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the binding variable
        _binding = CourseFinishedNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the button click listener using binding
        binding.continueButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt() - (2 * 32 * resources.displayMetrics.density).toInt()
            dialog.window?.setLayout(width, height)
            dialog.window?.setGravity(Gravity.CENTER)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    companion object {
        fun newInstance(): HalfScreenNotificationFragment {
            return HalfScreenNotificationFragment()
        }
    }
}
