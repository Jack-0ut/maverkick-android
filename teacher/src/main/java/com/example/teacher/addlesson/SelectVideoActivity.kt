package com.example.teacher.addlesson

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.example.teacher.databinding.ActivityVideoSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SelectVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoSelectionBinding
    private val viewModel: AddVideoLessonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId") // Fetch courseId from intent
        val language = intent.getStringExtra("language") // Fetch language from intent

        // Set the courseId and the languageCode in the ViewModel
        if (courseId != null && language != null) {
            viewModel.setCourseId(courseId)
            viewModel.setCourseLanguage(language)
        }
        binding.btnSelect.setOnClickListener {
            getContent.launch("video/*")
        }
    }

    /** Return the Uri of the chosen video **/
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val videoDuration = getVideoDuration(uri)
                val intent = Intent(this@SelectVideoActivity, AddLessonActivity::class.java).apply {
                    putExtra("VIDEO_URI", uri.toString())
                    putExtra("COURSE_ID", viewModel.courseId.value)
                    putExtra("VIDEO_DURATION", videoDuration)
                    putExtra("LANGUAGE_CODE", viewModel.language.value)
                }
                startActivity(intent)
            }
        }
    }

    /** Get the duration of the video in seconds */
    private suspend fun getVideoDuration(uri: Uri): Int = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@SelectVideoActivity, uri)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val timeInMs = time?.toLong() ?: 0L
        (timeInMs / 1000).toInt()
    }
}
