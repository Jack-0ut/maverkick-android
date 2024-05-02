package com.maverkick.student.course

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.CourseType
import com.maverkick.data.models.TextCourse
import com.maverkick.data.models.VideoCourse
import com.maverkick.student.R
import com.maverkick.student.adapters.CourseLessonAdapter
import com.maverkick.student.databinding.CourseOverviewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

/** Overview of the Course: picture, list of lessons and enroll button **/
@AndroidEntryPoint
class CourseOverviewActivity : AppCompatActivity() {
    private lateinit var binding: CourseOverviewBinding

    private val viewModel: CourseOverviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CourseOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId")
        val courseTypeName = intent.getStringExtra("courseType")

        if (courseId != null && courseTypeName != null) {
            try {
                val courseType = CourseType.valueOf(courseTypeName)
                viewModel.fetchCourseDetails(courseId)
                viewModel.fetchLessons(courseId, courseType)
            } catch (e: IllegalArgumentException) { }
        }

        val courseLessonAdapter = CourseLessonAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = courseLessonAdapter

        viewModel.courseDetails.observe(this) { course ->
            binding.courseName.text = course.courseName
            val lessonCount = course.numberLessons
            binding.numOfLessons.text = resources.getString(R.string.course_lessons_format, lessonCount)

            val imageUrl = when(course) {
                is VideoCourse -> course.poster
                is TextCourse -> course.poster
                else -> return@observe
            }

            Glide.with(this)
                .load(imageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        resource?.let {
                            // Obtain the intrinsic height of the drawable and calculate the aspect ratio
                            val imageHeight = it.intrinsicHeight
                            val imageWidth = it.intrinsicWidth
                            val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

                            adjustAppBarHeight(aspectRatio)
                        }
                        return false
                    }
                })
                .into(binding.coursePoster)
        }

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
        
        viewModel.lessons.observe(this) { lessons ->
            courseLessonAdapter.submitList(lessons)
        }

        viewModel.errorMessage.observe(this) { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    /** Adjust height based on the aspect ratio of the poster image **/
    private fun adjustAppBarHeight(aspectRatio: Float) {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val calculatedHeight = screenWidth / aspectRatio

        val screenHeight = displayMetrics.heightPixels
        val maxHeight = (screenHeight * 0.40).toInt()
        val finalHeight = min(calculatedHeight, maxHeight.toFloat())

        val params = binding.appBarLayout.layoutParams
        params.height = finalHeight.toInt()
        binding.appBarLayout.layoutParams = params
    }

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)
    }
}
