package com.maverkick.teacher.addlesson

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope

import com.maverkick.teacher.databinding.ActivityVideoSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SelectVideoActivity : AppCompatActivity() {

    private val viewModel: AddVideoLessonViewModel by viewModels()
    private lateinit var binding: ActivityVideoSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val courseId = intent.getStringExtra("courseId")
        val language = intent.getStringExtra("language")

        // Set the courseId and the languageCode in the ViewModel
        courseId?.let { viewModel.setCourseId(it) }
        language?.let { viewModel.setCourseLanguage(it) }

        binding.btnSelect.setOnClickListener {
            getContent.launch("video/*")
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val videoDuration = getVideoDuration(uri)
                val intent = Intent(this@SelectVideoActivity, AddLessonActivity::class.java).apply {
                    putExtras(bundleOf(
                        "VIDEO_URI" to uri.toString(),
                        "COURSE_ID" to viewModel.courseId.value,
                        "VIDEO_DURATION" to videoDuration,
                        "LANGUAGE_CODE" to viewModel.language.value
                    ))
                }
                startActivity(intent)
            }
        }
    }

    private suspend fun getVideoDuration(uri: Uri): Int = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this@SelectVideoActivity, uri)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val timeInMs = time?.toLong() ?: 0L
        (timeInMs / 1000).toInt()
    }
}
