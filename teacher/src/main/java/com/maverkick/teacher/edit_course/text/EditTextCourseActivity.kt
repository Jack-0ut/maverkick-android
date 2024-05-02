package com.maverkick.teacher.edit_course.text

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.Lesson
import com.maverkick.data.models.TextLesson
import com.maverkick.teacher.R
import com.maverkick.teacher.adapters.LessonDescriptionAdapter
import com.maverkick.teacher.databinding.ActivityTextCourseEditBinding
import com.maverkick.text_lesson.ui.TextLessonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditTextCourseActivity : AppCompatActivity(){
    private lateinit var binding: ActivityTextCourseEditBinding
    private val viewModel: EditTextCourseViewModel by viewModels()
    private lateinit var courseId: String

    private val lessonDescriptionAdapter = LessonDescriptionAdapter(object : OnItemClickListener<Lesson> {
        override fun onItemClick(item: Lesson) {
            when(item) {
                is TextLesson -> {
                    val textIntent = Intent(this@EditTextCourseActivity, TextLessonActivity::class.java).apply {
                        putExtra("courseId", item.courseId)
                        putExtra("lessonId", item.lessonId)
                        putExtra("title", item.title)
                        putExtra("content", item.content)
                    }
                    startActivity(textIntent)
                }
                else -> {}
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextCourseEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra("courseId").toString()

        viewModel.fetchCourse(courseId)
        viewModel.fetchLessons(courseId)

        binding.lessonList.layoutManager = LinearLayoutManager(this)
        binding.lessonList.adapter = lessonDescriptionAdapter

        viewModel.textCourse.observe(this) { course ->
            binding.courseName.text = course.courseName
            Glide.with(this).load(course.poster).into(binding.coursePoster)
        }

        viewModel.posterUri.observe(this) { uri ->
            Glide.with(this).load(uri).into(binding.coursePoster)
        }

        viewModel.lessons.observe(this) { lessons ->
            lessonDescriptionAdapter.submitList(lessons)
        }

        binding.publishButton.setOnClickListener { publishCourse() }
        binding.removeCourseButton.setOnClickListener{ showRemoveCourseDialog() }

        viewModel.textCourse.observe(this) { course ->
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

    /** Get new poster from gallery */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePoster(uri)
        }
    }

    /** Publish the course and show a Snackbar message based on the result **/
    private fun publishCourse() {
        viewModel.publishCourse(courseId)
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
                        finish()
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