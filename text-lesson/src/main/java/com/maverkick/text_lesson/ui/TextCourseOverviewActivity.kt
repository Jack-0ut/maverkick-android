package com.maverkick.text_lesson.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.CourseType
import com.maverkick.text_lesson.adapters.LessonDescriptionAdapter
import com.maverkick.text_lesson.databinding.ActivityTextCourseOverviewBinding
import com.maverkick.text_lesson.viewmodels.TextCourseOverviewViewModel
import dagger.hilt.android.AndroidEntryPoint

/** Activity that display the overview of the course: title, number of lessons
 * and a list of lessons in the short format
 **/
@AndroidEntryPoint
class TextCourseOverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextCourseOverviewBinding
    private val viewModel: TextCourseOverviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the binding object
        binding = ActivityTextCourseOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_accent_light)

        // Suppose you have the course ID from an Intent or some other source
        val courseId = intent.getStringExtra("courseId")

        val courseTypeString = intent.getStringExtra("courseType")

        if (courseId != null && courseTypeString != null) {
            Log.d("CourseDetailsActivityX", "CourseId is not null: $courseId")

            try {
                val courseType = CourseType.valueOf(courseTypeString)
                Log.d("CourseDetailsActivityX", "Calling fetchCourseInformation with courseId: $courseId and courseType: $courseType")
                viewModel.fetchCourseInformation(courseId, courseType)
                Log.d("CourseDetailsActivityX", "Called fetchCourseInformation")

                Log.d("CourseDetailsActivityX", "Calling fetchLessons with courseId: $courseId and courseType: $courseType")
                viewModel.fetchLessons(courseId, courseType)
                Log.d("CourseDetailsActivityX", "Called fetchLessons")
            } catch (e: IllegalArgumentException) {
                Log.w("CourseDetailsActivityX", "Invalid CourseType string: $courseTypeString")
            }

        } else {
            Log.w("CourseDetailsActivityX", "CourseId or CourseTypeString is null")
        }


        // navigate back to the home screen
        viewModel.enrollmentComplete.observe(this) { enrollmentComplete ->
            if (enrollmentComplete) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://student/studentHomeFragment"))
                viewModel.resetEnrollmentCompleteFlag()
                startActivity(intent)
                finish()
            }
        }

        viewModel.course.observe(this) { course ->
            // Update course information in the UI
            binding.courseTitle.text = course?.courseName
            binding.lessonCount.text = course?.numberLessons.toString()
        }

        viewModel.lessons.observe(this) { lessons ->
            // Update the RecyclerView with the list of lessons
            binding.lessonList.layoutManager = LinearLayoutManager(this)
            binding.lessonList.adapter = LessonDescriptionAdapter(lessons)
        }

        // Handle Enroll button click
        binding.buttonEnroll.setOnClickListener {
            if (courseId != null) {
                viewModel.enrollStudentInTextCourse(courseId)
            } else {
                Snackbar.make(binding.root, "Can't get the id of the course. Try again, please!", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.main_color)
    }
}
