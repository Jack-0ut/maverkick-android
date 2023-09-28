package com.maverkick.text_lesson.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.android.material.snackbar.Snackbar
import com.maverkick.common.R
import com.maverkick.text_lesson.databinding.ActivityPersonalizedCourseCreationBinding
import com.maverkick.text_lesson.viewmodels.PersonalizedCourseCreationViewModel
import com.maverkick.text_lesson.workers.CheckCourseGenerationProgressWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity in which student writes the prompt describing the course he would like to get and
 * also it should specify particular parameters such as
 **/
@AndroidEntryPoint
class CreatePersonalizedCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalizedCourseCreationBinding
    private val viewModel: PersonalizedCourseCreationViewModel by viewModels()

    companion object {
        const val REQUEST_NOTIFICATION_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalizedCourseCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDropdown()
        setupObservers()

        binding.buttonGenerateCourse.setOnClickListener {
            if (canStartGeneration()) {
                initiateCourseGeneration()
            }
        }
    }

    private fun setupObservers() {
        viewModel.courseGenerationTries.observe(this) { result ->
            result.onSuccess { tries ->
                if (tries <= 0) {
                    showErrorSnackbar("You have no course generation tries left.")
                }
            }.onFailure { error ->
                showErrorSnackbar("Error fetching course generation tries: ${error.message}")
            }
        }

        viewModel.courseGenerationResult.observe(this) { result ->
            result.onSuccess { response ->
                if (response.isSuccessful) {
                    val courseId = response.body()?.courseId ?: ""
                    enqueueWorkManagerTask(courseId)
                    finish()
                } else {
                    showErrorSnackbar("Error starting course generation.")
                }
            }.onFailure {
                showErrorSnackbar("Error starting course generation")
            }
        }
    }

    private fun initiateCourseGeneration() {
        val courseDescription = binding.inputCourseDescription.text.toString()
        val selectedLanguage = binding.courseLanguage.editText?.text.toString()

        when {
            courseDescription.isBlank() -> showErrorSnackbar("Please enter a course description.")
            selectedLanguage.isBlank() -> showErrorSnackbar("Please select a language.")
            else -> {
                val userId = viewModel.getUserId() ?: run {
                    showErrorSnackbar("User ID not found. Cannot generate course.")
                    return
                }
                viewModel.initiateCourseGeneration(userId, courseDescription, selectedLanguage)
            }
        }
    }

    private fun canStartGeneration(): Boolean {
        val workInfo = WorkManager.getInstance(this)
            .getWorkInfosByTag("COURSE_GENERATION_PROGRESS").get()

        val isWorkerRunning = workInfo.any {
            it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
        }

        if (isWorkerRunning) {
            showErrorSnackbar("Course generation is already in progress.")
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            requestNotificationPermission()
            return false
        }
        return true
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_NOTIFICATION_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateCourseGeneration()  // Retry course generation after permission granted
            } else {
                showErrorSnackbar("Notification permission denied. Cannot proceed with course generation.")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun enqueueWorkManagerTask(courseId: String) {
        // Create a unique work name based on the courseId
        val uniqueWorkName = "checkCourseProgress_$courseId"

        // Enqueue CourseProgressCheckWorker to check progress every 20 minutes
        val progressCheckRequest = OneTimeWorkRequestBuilder<CheckCourseGenerationProgressWorker>()
            .setInputData(workDataOf("courseId" to courseId))
            .addTag("COURSE_GENERATION_PROGRESS")
            .build()

        // Use ExistingWorkPolicy.KEEP to ensure only one worker instance for each courseId runs at a time
        WorkManager.getInstance(this).beginUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            progressCheckRequest
        ).enqueue()
    }

    private fun initializeDropdown() {
        val languages = resources.getStringArray(com.maverkick.text_lesson.R.array.text_course_languages)
        val adapter = ArrayAdapter(this, R.layout.language_list_item, languages)
        (binding.courseLanguage.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.maverkick_white))
            .show()
    }
}
