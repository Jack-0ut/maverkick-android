package com.maverkick.tasks.quiz_summary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maverkick.tasks.R
import com.maverkick.tasks.databinding.ActivityPostLessonSummaryBinding
import com.mikhaellopez.circularprogressbar.CircularProgressBar

/**
 * PostLessonSummaryActivity displays the results after a lesson's exercises.
 *
 * This activity showcases the percentage of correct answers in a circular progress bar
 * and provides feedback to the user based on their performance. The feedback ranges from
 * "Excellent" for a perfect score to encouragements for lesser scores.
 *
 * It receives the total number of questions and the count of correct answers from an intent.
 *
 * ## Usage
 * To use this activity, ensure you pass the following intent extras:
 * - TOTAL_QUESTIONS: Int - The total number of questions in the quiz.
 * - CORRECT_ANSWERS: Int - The number of questions the user got right.
 *
 * ## Components:
 * - Circular ProgressBar: To visually show the percentage of correct answers.
 * - TextViews: To display the exact percentage and feedback message.
 *
 * ## Note:
 * For customizing feedback messages, update the `feedback_messages` string array resource.
 */
class PostLessonSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostLessonSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostLessonSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the counts from the intent, defaulting to 0 if not provided.
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)

        // Calculate the percentage of correct answers.
        val percentage = (correctAnswers * 100) / totalQuestions

        // Populate UI components.
        binding.percentageText.text = getString(R.string.percentage_format, percentage)
        binding.feedbackText.text = getFeedback(totalQuestions, correctAnswers)

        // Animate CircularProgressBar using the official documentation.
        binding.progressBar.apply {
            progressMax = 100f

            // Set Width
            progressBarWidth = 7f
            backgroundProgressBarWidth = 3f
            roundBorder = true
            startAngle = 0f
            progressDirection = CircularProgressBar.ProgressDirection.TO_RIGHT

            // Animate to the percentage value
            setProgressWithAnimation(percentage.toFloat(), 1000)
        }

        binding.nextLessonButton.setOnClickListener {
            val redirectIntent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://student/studentHomeFragment"))
            startActivity(redirectIntent)
            finish()
        }
    }

    /**
     * Determine the feedback message based on the percentage of correct answers.
     *
     * This function compares the user's percentage to predefined thresholds
     * and returns a feedback message from the `feedback_messages` string array resource.
     *
     * @param totalQuestions The total number of questions.
     * @param correctAnswers The number of correct answers.
     * @return A feedback message based on the user's performance.
     */
    private fun getFeedback(totalQuestions: Int, correctAnswers: Int): String {
        val percentage = (correctAnswers * 100) / totalQuestions

        return when {
            percentage == 100 -> "Excellent! You got all the answers right!"
            percentage >= 80 -> "Great job! You know this topic well."
            percentage >= 60 -> "Good effort! But there's some room for improvement."
            percentage >= 40 -> "It seems you might need some more practice."
            else -> "Keep trying and you'll get there!"
        }
    }
}
