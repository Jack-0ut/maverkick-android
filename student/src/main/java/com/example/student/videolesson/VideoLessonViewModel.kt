package com.example.student.videolesson

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.repositories.CourseStatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**ViewModel that responsible for interactions that happens, while student watching the video.
 * It's saving the assessment rate, maybe some other interactions
 **/
@HiltViewModel
class VideoLessonViewModel @Inject constructor(
    private val courseStatisticsRepository: CourseStatisticsRepository
) : ViewModel() {

    private val _lessonRating = MutableLiveData<Int>()

    /** Store rating locally**/
    fun setLessonRating(rating: Int) {
        _lessonRating.value = rating
    }

    /** Update the rating for the course in the database **/
    fun updateRatings(courseId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val rating = _lessonRating.value ?: return
        courseStatisticsRepository.updateRatings(courseId, rating, onSuccess, onFailure)
    }
}
