package com.example.video_lesson.student

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.GridView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.example.chat_helper.AskQuestionDialogFragment
import com.example.video_lesson.AbstractVideoActivity
import com.example.video_lesson.ExoPlayerWrapper
import com.example.video_lesson.R
import com.example.video_lesson.VideoPlayerInterface
import com.example.video_lesson.adapters.EmojiAdapter
import com.example.video_lesson.databinding.ActivityVideoLessonBinding
import com.maverkick.data.models.CourseType
import com.maverkick.tasks.ExerciseActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the VideoActivity class. It is responsible for displaying the video lessons to the students.
 * The activity allows students to interact with the video by pausing and playing it.
 * They can also ask questions while the video is playing by clicking on the 'ASK' icon.
 */
@AndroidEntryPoint
class VideoLessonActivity : AbstractVideoActivity() {

    private lateinit var binding: ActivityVideoLessonBinding
    private val viewModel: VideoLessonViewModel by viewModels()

    private lateinit var lessonId: String
    private lateinit var videoUri: String
    private lateinit var transcription: String
    private lateinit var title: String
    private lateinit var courseId: String

    private val emojis = listOf("🤩", "😊", "😐", "😕", "😡")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.main_tone_color)

        intent?.extras?.let {
            lessonId = it.getString("lessonId", "")
            videoUri = it.getString("videoUri", "")
            transcription = it.getString("transcription", "")
            title = it.getString("title", "")
            courseId = it.getString("courseId", "")
        }

        binding.apply {
            videoTitle.text = title
            videoView.keepScreenOn = true
            chatButton.setOnClickListener { onChatButtonClick() }
            reactionButton.setOnClickListener { onReactionButtonClick() }
        }
    }

    private fun onChatButtonClick() {
        player?.playWhenReady = false
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)

        val dialogFragment = AskQuestionDialogFragment().apply {
            arguments = bundleOf(
                "courseId" to courseId,
                "transcription" to transcription,
                "lessonId" to lessonId
            )
            dismissListener = { window.statusBarColor = ContextCompat.getColor(this@VideoLessonActivity, com.maverkick.common.R.color.main_tone_color) }
        }
        dialogFragment.show(supportFragmentManager, "AskQuestionDialogFragment")
    }

    private fun onReactionButtonClick() {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.emoji_picker)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<GridView>(R.id.grid_view).apply {
                adapter = EmojiAdapter(this@VideoLessonActivity, R.layout.item_emoji, emojis) { _, position ->
                    dismiss()
                    viewModel.setLessonRating(5 - position)
                }
            }
        }
        dialog.show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.videoTitle.visibility = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            View.GONE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            View.VISIBLE
        }
    }

    override val videoUrl: String
        get() = videoUri

    override fun onPlayerEnd() {
        val intent = ExerciseActivity.newIntent(this, courseId, lessonId, CourseType.VIDEO)
        startActivity(intent)
        finish()
        viewModel.updateRatings(courseId, {}, {})
    }

    override fun initializeBinding() {
        binding = ActivityVideoLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun setPlayerView(player: VideoPlayerInterface) {
        val exoSimplePlayer = player as? ExoPlayerWrapper
        binding.videoView.player = exoSimplePlayer?.playerInstance
    }

    override fun createPlayerInstance(): VideoPlayerInterface {
        return ExoPlayerWrapper(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)
    }
}
