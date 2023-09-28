package com.maverkick.text_lesson.ui

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chat_helper.AskQuestionDialogFragment
import com.maverkick.data.models.CourseType
import com.maverkick.tasks.ExerciseActivity
import com.maverkick.text_lesson.databinding.ActivityTextLessonBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * TextLessonActivity is responsible for displaying a specific text lesson within a course.
 * It fetches the details of the lesson based on the provided course and lesson IDs and updates the UI accordingly.
 *
 * This activity is part of the learning module and represents one of the main ways students can engage with text-based content.
 *
 * The course and lesson IDs should be passed via the intent that starts this activity.
 */
@AndroidEntryPoint
class TextLessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextLessonBinding
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTextLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_white)

        webView = binding.lessonContent

        val courseId = intent.getStringExtra("courseId") ?: ""
        val lessonId = intent.getStringExtra("lessonId") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""

        binding.lessonTitle.text = title

        val htmlContent = convertToHtml(content)

        val finalContent = """
        <html><head>
        <style>
            body {
                font-family: 'Georgia', serif; /* Change to 'Literata', 'Bookerly', 'Roboto' or other fonts if you prefer */
                font-size: 18px;
                line-height: 1.6;
                color: #333;
                background-color: #FDFCFC;
            }
            h1, h2, h3 {
                margin-top: 20px;
                margin-bottom: 10px;
            }
            h1 {
                font-size: 32px;
            }
            h2 {
                font-size: 28px;
            }
            h3 {
                font-size: 24px;
            }
        </style>
        <script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML'>
        </script>
        </head><body>
        $htmlContent
        <script type='text/javascript'>
        MathJax.Hub.Queue(['Typeset',MathJax.Hub]);
        </script></body></html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, finalContent, "text/html", "UTF-8", null)

        setupAskButton(courseId, lessonId, content)
        setupFinishButton(courseId, lessonId)
    }

    private fun setupAskButton(courseId: String, lessonId: String, content: String) {
        binding.askButton.setOnClickListener {
            window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)

            val dialogFragment = AskQuestionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("courseId", courseId)
                    putString("transcription", content)
                    putString("lessonId", lessonId)
                }
            }
            dialogFragment.dismissListener = {
                window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_white)
            }
            dialogFragment.show(supportFragmentManager, "AskQuestionDialogFragment")
        }
    }

    private fun setupFinishButton(courseId: String, lessonId: String) {
        binding.finishButton.setOnClickListener {
            val intent = ExerciseActivity.newIntent(this, courseId, lessonId, CourseType.TEXT)
            startActivity(intent)
            finish()
        }
    }
    fun convertToHtml(markdown: String): String {
        val lines = markdown.split("\n")
        val htmlLines = mutableListOf<String>()

        for (line in lines) {
            when {
                line.startsWith("### ") -> htmlLines.add("<h3>${line.substring(4)}</h3>")
                line.startsWith("## ") -> htmlLines.add("<h2>${line.substring(3)}</h2>")
                line.startsWith("# ") -> htmlLines.add("<h1>${line.substring(2)}</h1>")
                else -> htmlLines.add(line)
            }
        }

        return htmlLines.joinToString(separator = "<br/>")
    }

}
