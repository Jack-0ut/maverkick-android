package com.example.teacher.addlesson

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.teacher.databinding.ActivityVideoSelectionBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for defining the source of video for video-lesson
 * Here teacher should choose where's that video file and
 * then choose the file itself
 **/
@AndroidEntryPoint
class SelectVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoSelectionBinding
    private val viewModel: AddVideoLessonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the courseId from the intent extras
        val courseId = intent.getStringExtra("COURSE_ID")

        // Set the courseId in the ViewModel
        if (courseId != null) {
            viewModel.setCourseId(courseId)
        }

        binding.btnSelect.setOnClickListener {
            getContent.launch("video/*")
        }
    }

    /** Return the Uri of the chosen video **/
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, AddLessonActivity::class.java)
            intent.putExtra("VIDEO_URI", uri.toString())
            intent.putExtra("COURSE_ID", viewModel.courseId.value)
            startActivity(intent)
        }
    }

}
