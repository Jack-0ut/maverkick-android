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
        window.statusBarColor = ContextCompat.getColor(this, com.maverkick.common.R.color.maverkick_main)

        webView = binding.lessonContent
        webView.settings.javaScriptEnabled = true

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
                font-family: 'Georgia', serif; /* Add your preferred font */
                font-size: 18px;
                line-height: 1.6;
                color: #333;
                background-color: #FDFCFC;
            }
            h1, h2, h3 {
                margin-top: 0px;
                margin-bottom: 0px;
            }
            h1 {
                font-size: 20px;
            }
            h2 {
                font-size: 18px;
            }
            h3 {
                font-size: 16px;
            }
            strong {
                font-weight: bold;
            }
            pre {
                white-space: pre-wrap;       /* Since CSS 2.1 */
                white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
                white-space: -pre-wrap;      /* Opera 4-6 */
                white-space: -o-pre-wrap;    /* Opera 7 */
                word-wrap: break-word;       /* Internet Explorer 5.5+ */
            }
            
            code {
                font-family: 'Courier New', Courier, monospace; /* or any other monospaced font */
            }
        </style>
        <link href='https://cdnjs.cloudflare.com/ajax/libs/prism/1.25.0/themes/prism.css' rel='stylesheet' />
        <script src='https://cdnjs.cloudflare.com/ajax/libs/prism/1.25.0/prism.js'></script>
        <script type="text/x-mathjax-config">
            MathJax.Hub.Config({
              tex2jax: {
                inlineMath: [['$','$'], ['\\(','\\)']],
                processEscapes: true
              }
            });
        </script>
        <script type="text/javascript" async
            src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.7/MathJax.js?config=TeX-MML-AM_CHTML">
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

    private fun convertToHtml(markdown: String): String {
        val lines = markdown.split("\n")
        val htmlLines = mutableListOf<String>()

        var inCodeBlock = false

        for (line in lines) {
            when {
                line.startsWith("```") -> {
                    inCodeBlock = !inCodeBlock
                    if (inCodeBlock) {
                        htmlLines.add("<pre><code>")
                    } else {
                        htmlLines.add("</code></pre>")
                    }
                }
                inCodeBlock -> {
                    htmlLines.add(line)
                }
                line.startsWith("#### ") -> htmlLines.add("<h3>${line.replaceFirst("#### ", "")}</h3>")
                line.startsWith("### ") -> htmlLines.add("<h3>${line.replaceFirst("### ", "")}</h3>")
                line.startsWith("## ") -> htmlLines.add("<h3>${line.replaceFirst("## ", "")}</h3>")
                line.startsWith("# ") -> htmlLines.add("<h2>${line.replaceFirst("# ", "")}</h2>")
                line.startsWith("[") && line.endsWith("]") -> {
                    val sectionContent = line.substring(1, line.length - 1)
                    htmlLines.add("<div class='section'><em>$sectionContent</em></div>")
                }
                line.contains("**") -> {
                    var modifiedLine = line
                    val regex = Regex("\\*\\*(.+?)\\*\\*")
                    val matchResult = regex.findAll(line)
                    matchResult.forEach { match ->
                        modifiedLine = modifiedLine.replace(match.value, "<strong>${match.groups[1]?.value}</strong>")
                    }
                    htmlLines.add(modifiedLine)
                }
                line.contains("(") && line.contains(")") -> {
                    // If the line contains LaTeX formula (between parentheses), wrap it with appropriate MathJax delimiters
                    var modifiedLine = line
                    val regex = Regex("\\((.+?)\\)")
                    val matchResult = regex.findAll(line)
                    matchResult.forEach { match ->
                        modifiedLine = modifiedLine.replace(match.value, "\\(${match.groups[1]?.value}\\)")
                    }
                    htmlLines.add(modifiedLine)
                }
                else -> htmlLines.add(line)
            }
        }
        return htmlLines.joinToString(separator = "<br/>")
    }

}
