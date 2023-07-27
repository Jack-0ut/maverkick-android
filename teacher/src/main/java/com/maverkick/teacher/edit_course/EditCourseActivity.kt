package com.maverkick.teacher.edit_course

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
import com.maverkick.teacher.adapters.LessonAdapter
import com.maverkick.teacher.addlesson.SelectVideoActivity
import com.maverkick.teacher.databinding.ActivityCourseEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment, where Teacher will be interacting with the course,
 * updating some things, uploading new videos and so on
 **/
@AndroidEntryPoint
class EditCourseActivity : AppCompatActivity(),LessonAdapter.OnLessonClickListener {
    private lateinit var binding: ActivityCourseEditBinding

    private val viewModel: EditCourseViewModel by viewModels()

    private lateinit var courseId: String
    private val lessonAdapter = LessonAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        courseId = intent.getStringExtra("courseId").toString()

        // Fetch the necessary data from the database
        viewModel.fetchCourse(courseId)
        viewModel.fetchLessons(courseId)

        // Set up RecyclerView
        binding.lessonList.layoutManager = LinearLayoutManager(this)
        binding.lessonList.adapter = lessonAdapter
        lessonAdapter.setOnLessonClickListener(this)

        viewModel.course.observe(this) { course ->
            // Update course related UI
            binding.courseName.text = course.courseName

            Glide.with(this).load(course.poster).into(binding.coursePoster)

            // Clear old chips if any before adding new ones
            binding.tags.removeAllViews()

            // Create a new Chip for each tag and add it to the ChipGroup
            course.tags.forEach { tag ->
                val chip = Chip(this)
                chip.text = tag
                chip.isCheckable = false
                chip.isCloseIconVisible = false
                binding.tags.addView(chip)
            }
        }

        // Observe the posterUri LiveData
        viewModel.posterUri.observe(this) { uri ->
            // Load the poster image from the updated Uri
            Glide.with(this).load(uri).into(binding.coursePoster)
        }

        viewModel.lessons.observe(this) { lessons ->
            lessonAdapter.submitList(lessons)
        }

        binding.publishButton.setOnClickListener {
           publishCourse()
        }

        binding.removeCourseButton.setOnClickListener{
            showRemoveCourseDialog()
        }

        viewModel.course.observe(this) { course ->
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
            viewModel.course.value?.let { course ->
                val intent = Intent(this, SelectVideoActivity::class.java)
                intent.putExtra("courseId", courseId)
                intent.putExtra("language", course.language)
                startActivity(intent)
            } ?: run {
                // Handle the error if the course is null. For example, show an error message to the user.
            }
        }

        binding.editPosterIcon.setOnClickListener{
            getContent.launch("image/*")
        }

    }

    /** Publish the course and show a Snackbar message based on the result **/
    private fun publishCourse() {
        viewModel.publishCourse(courseId,
            onSuccess = {
                Snackbar.make(binding.root, "Course has been activated", Snackbar.LENGTH_LONG).show()
            },
            onFailure = { exception ->
                Snackbar.make(binding.root, exception.message ?: "Course hasn't been activated", Snackbar.LENGTH_LONG).show()
            })
    }

    /** Get new poster from gallery */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePoster(uri,
                {
                    Snackbar.make(binding.root, "Poster has been updated.", Snackbar.LENGTH_SHORT).show()
                },
                { exception ->
                    Snackbar.make(binding.root, "Failed to update the poster: ${exception.message}", Snackbar.LENGTH_SHORT).show()
                }
            )
        }
    }

    /** When click on the particular lesson toggle icon **/
    override fun onLessonClick(lessonId: String, position: Int) {
        lessonAdapter.onLessonClick(lessonId, position)
    }
    /** Dialog to ensure that teacher really want to remove course */
    private fun showRemoveCourseDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove Course")
            .setMessage("Do you want to remove your course? It will no longer be recommended for new students, but those who are enrolled will have an opportunity to finish it.")
            .setPositiveButton("Go ahead") { _, _ ->
                viewModel.deleteCourse(
                    onSuccess = {
                        Snackbar.make(binding.root,"The course has been deleted!",5000).show()
                        finish()
                    },
                    onFailure = {
                        Snackbar.make(binding.root,"Can't delete the Course, try again!",5000).show()
                    }
                )
            }
            .setNegativeButton("No", null)
            .show()
    }
}
