package com.example.student.fragments

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.student.adapters.CourseLessonAdapter
import com.example.student.databinding.ActivityCourseDetailsBinding
import com.example.student.viewmodels.CourseDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity that display the information about the course to the Student
 **/
@AndroidEntryPoint
class CourseDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseDetailsBinding

    private val viewModel: CourseDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId")
        courseId?.let { id ->
            viewModel.fetchCourseDetails(id)
            viewModel.fetchLessons(id)
        }

        // Initialize  CourseLessonAdapter
        val courseLessonAdapter = CourseLessonAdapter()
        binding.lessonsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.lessonsRecyclerView.adapter = courseLessonAdapter

        // navigate back to the home screen
        binding.enrollButton.setOnClickListener {
            if (courseId != null) {
                viewModel.enrollStudent(courseId)
                // Replace with the appropriate action
                finish()  // This will take you back to the previous activity
            } else {
                Toast.makeText(this,"Can't get the id of the course.Try again,please!",Toast.LENGTH_SHORT).show()
            }
        }

        // Observe course details LiveData
        viewModel.course.observe(this) { course ->
            // Update UI with course details
            binding.courseTitle.text = course.courseName
        }

        // Observe lessons LiveData
        viewModel.lessons.observe(this) { lessons ->
            courseLessonAdapter.submitList(lessons)
        }

        // Observe teacher LiveData
        viewModel.teacher.observe(this) { teacher ->
            // Update UI with teacher name
            binding.teacherName.text = teacher.fullName
        }

        // Observe user LiveData
        viewModel.user.observe(this) { user ->
            // Update UI with teacher profile picture
            Glide.with(this).load(user?.profilePicture).into(binding.teacherImage)
        }
    }
}
