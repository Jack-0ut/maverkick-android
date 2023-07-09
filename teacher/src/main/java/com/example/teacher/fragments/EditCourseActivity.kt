package com.example.teacher.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.teacher.adapters.LessonAdapter
import com.example.teacher.addlesson.SelectVideoActivity
import com.example.teacher.databinding.ActivityCourseEditBinding
import com.example.teacher.viewmodels.EditCourseViewModel
import com.google.android.material.chip.Chip
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

    /** Get new poster from gallery */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            viewModel.updatePoster(uri)
        }
    }

    /** When click on the particular lesson toggle icon **/
    override fun onLessonClick(lessonId: String, position: Int) {
        lessonAdapter.onLessonClick(lessonId, position)
    }
}
