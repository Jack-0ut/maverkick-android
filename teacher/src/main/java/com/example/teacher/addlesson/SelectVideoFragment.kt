package com.example.teacher.addlesson

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.teacher.databinding.FragmentVideoSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for defining the source of video for video-lesson
 * Here teacher should choose where's that video file and
 * then choose the file itself
 **/
@AndroidEntryPoint
class SelectVideoFragment : Fragment() {

    private var _binding: FragmentVideoSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddVideoLessonViewModel by viewModels()
    private val args: SelectVideoFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Later, you can use args to access the passed arguments
        val courseId = args.courseId
        val language = args.language

        // Set the courseId and the languageCode in the ViewModel
        viewModel.setCourseId(courseId)
        viewModel.setCourseLanguage(language)

        binding.btnSelect.setOnClickListener {
            getContent.launch("video/*")
        }
    }

    /** Return the Uri of the chosen video **/
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val videoDuration = getVideoDuration(requireContext(), uri)
                val intent = Intent(requireContext(), AddLessonActivity::class.java).apply {
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
    private suspend fun getVideoDuration(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val timeInMs = time?.toLong() ?: 0L
        (timeInMs / 1000).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
