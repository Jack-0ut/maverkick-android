package com.maverkick.student.course

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.maverkick.student.adapters.CourseLessonAdapter
import com.maverkick.student.databinding.ActivityCourseDetailsBinding
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

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_accent_light)

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
        viewModel.enrollmentComplete.observe(this) { enrollmentComplete ->
            if (enrollmentComplete) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://student/studentHomeFragment"))
                startActivity(intent)
                viewModel.resetEnrollmentCompleteFlag()
            }
        }

        // Observe isAlreadyEnrolled LiveData
        viewModel.isAlreadyEnrolled.observe(this) { isAlreadyEnrolled ->
            if (isAlreadyEnrolled) {
                // Hide the enroll button if the course is already enrolled.
                binding.enrollButton.visibility = View.GONE
            } else {
                // Otherwise, show the enroll button.
                binding.enrollButton.visibility = View.VISIBLE
            }
        }

        // enroll in the course
        binding.enrollButton.setOnClickListener {
            if (courseId != null) {
                viewModel.enrollStudent(courseId)
            } else {
                Snackbar.make(binding.root, "Can't get the id of the course. Try again, please!", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Observe course details LiveData
        viewModel.videoCourse.observe(this) { course ->
            // Update UI with course details
            binding.courseTitle.text = course.courseName
            binding.lessonCount.text = course.numberLessons.toString()
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

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.main_color)
    }
}
