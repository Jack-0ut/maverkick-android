package com.maverkick.teacher.edit_course.video

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.shared_ui.OnItemClickListener
import com.example.video_lesson.teacher.TeacherVideoLessonActivity
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.Lesson
import com.maverkick.data.models.VideoLesson
import com.maverkick.teacher.R
import com.maverkick.teacher.adapters.LessonDescriptionAdapter
import com.maverkick.teacher.addlesson.SelectVideoActivity
import com.maverkick.teacher.databinding.ActivityVideoCourseEditBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment, where Teacher will be interacting with the course,
 * updating some things, uploading new videos and so on
 **/
@AndroidEntryPoint
class EditVideoCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoCourseEditBinding
    private val viewModel: EditVideoCourseViewModel by viewModels()
    private lateinit var courseId: String

    private val lessonDescriptionAdapter = LessonDescriptionAdapter(object : OnItemClickListener<Lesson> {
        override fun onItemClick(item: Lesson) {
            when(item) {
                is VideoLesson -> {
                    val videoIntent = Intent(this@EditVideoCourseActivity, TeacherVideoLessonActivity::class.java).apply {
                        putExtra(TeacherVideoLessonActivity.EXTRA_VIDEO_URL, item.videoUrl)
                    }
                    startActivity(videoIntent)
                }
                else -> {}
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCourseEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra("courseId").toString()

        // Fetch the necessary data from the database
        viewModel.fetchCourse(courseId)
        viewModel.fetchLessons(courseId)

        binding.lessonList.layoutManager = LinearLayoutManager(this)
        binding.lessonList.adapter = lessonDescriptionAdapter

        viewModel.videoCourse.observe(this) { course ->
            binding.courseName.text = course.courseName

            Glide.with(this).load(course.poster).into(binding.coursePoster)

            // Clear old chips if any before adding new ones
            binding.tags.removeAllViews()

            course.tags.forEach { tag ->
                val chip = Chip(this)
                chip.text = tag
                chip.isCheckable = false
                chip.isCloseIconVisible = false
                binding.tags.addView(chip)
            }
        }

        viewModel.posterUri.observe(this) { uri ->
            Glide.with(this).load(uri).into(binding.coursePoster)
        }

        viewModel.lessons.observe(this) { lessons ->
            lessonDescriptionAdapter.submitList(lessons)
        }

        binding.publishButton.setOnClickListener { publishCourse() }
        binding.removeCourseButton.setOnClickListener{ showRemoveCourseDialog() }

        viewModel.videoCourse.observe(this) { course ->
            binding.publishSwitch.isChecked = course.published

            // Update the color of the switch based on the course's publication status
            val switchColor = if (course.published) {
                ContextCompat.getColor(this, com.maverkick.common.R.color.green)
            } else {
                ContextCompat.getColor(this, com.maverkick.common.R.color.red)
            }
            binding.publishSwitch.thumbTintList = ColorStateList.valueOf(switchColor)

            if (course.published) {
                binding.publishButton.visibility = View.GONE
                binding.removeCourseButton.visibility = View.GONE
            } else {
                binding.publishButton.visibility = View.VISIBLE
                binding.removeCourseButton.visibility = View.VISIBLE
            }
        }

        // add new lesson button
        binding.addLessonButton.setOnClickListener {
            viewModel.videoCourse.value?.let { course ->
                val intent = Intent(this, SelectVideoActivity::class.java)
                intent.putExtra("courseId", courseId)
                intent.putExtra("language", course.language)
                startActivity(intent)
            } ?: run {}
        }

        binding.editPosterIcon.setOnClickListener{
            getContent.launch("image/*")
        }

        viewModel.success.observe(this) { successMessage ->
            showSnackbar(successMessage)
        }

        viewModel.errors.observe(this) { errorMessage ->
            showSnackbar(errorMessage)
        }
    }

    /** Publish the course and show a Snackbar message based on the result **/
    private fun publishCourse() {
        viewModel.publishCourse(courseId)
    }

    /** Get new poster from gallery */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePoster(uri)
        }
    }

    /** Dialog to ensure that teacher really want to remove course */
    private fun showRemoveCourseDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_remove_course_title))
            .setMessage(getString(R.string.dialog_remove_course_message))
            .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                viewModel.deleteCourse(
                    onSuccess = { successMessage ->
                        Snackbar.make(binding.root, successMessage, Snackbar.LENGTH_LONG).show()
                        finish() // Close the activity on success
                    },
                    onFailure = { errorMessage ->
                        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton(getString(R.string.dialog_negative_button), null)
            .show()
    }

    /** Function to easily show Snackbar messages **/
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
