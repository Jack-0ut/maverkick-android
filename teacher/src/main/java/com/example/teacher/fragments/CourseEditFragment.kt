package com.example.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.teacher.databinding.FragmentCourseEditBinding

/**
 * Fragment, where Teacher will be interacting with the course,
 * updating some things, uploading new videos and so on
 **/
class CourseEditFragment : Fragment() {
    private var _binding: FragmentCourseEditBinding? = null
    private val binding get() = _binding!!

    private val args: CourseEditFragmentArgs by navArgs()
    private lateinit var courseId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseEditBinding.inflate(inflater, container, false)

        courseId = args.courseId
        binding.courseId.text = courseId

        return binding.root
    }

}
