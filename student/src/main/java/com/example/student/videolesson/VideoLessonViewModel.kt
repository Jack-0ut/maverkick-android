package com.example.student.videolesson

import androidx.lifecycle.ViewModel
import com.example.data.repositories.DailyLearningPlanRepository
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoLessonViewModel @Inject constructor(
    private val dailyLearningPlanRepository: DailyLearningPlanRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel(){



}