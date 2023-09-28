package com.maverkick.student.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.models.Course
import com.maverkick.data.models.VideoCourse
import com.maverkick.data.repositories.StudentRepository
import com.maverkick.data.repositories.TextCourseRepository
import com.maverkick.data.repositories.VideoCourseRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the GalleryFragment
 * This viewModel class will be responsible for
 * the displaying the search results for course searching and caching the results
 **/
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val videoCourseRepository: VideoCourseRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val studentRepository: StudentRepository,
    private val textCourseRepository: TextCourseRepository
) : ViewModel() {

    private val _courses = MutableLiveData<List<VideoCourse>>()
    val courses: LiveData<List<VideoCourse>> get() = _courses

    private val _generatedCourses = MutableLiveData<List<Course>>()
    val generatedCourses: LiveData<List<Course>> = _generatedCourses

    private val _isNewDataAvailable = MutableLiveData<Boolean>()
    val isNewDataAvailable: LiveData<Boolean> get() = _isNewDataAvailable

    init {
        fetchGeneratedTextCourses()
        fetchCourses()
    }

    /** Fetch all the courses **/
    private fun fetchCourses() {
        viewModelScope.launch {
            runCatching {
                // Fetch the list of courses from the repository
                videoCourseRepository.getPublishedCourses()
            }.onSuccess { fetchedCourses ->
                // Update the _courses LiveData with the fetched courses
                _courses.value = fetchedCourses
            }.onFailure {}
        }
    }

    fun fetchGeneratedTextCourses() {
        Log.d("CourseGeneratedEvent", "fetchGeneratedTextCourses called")
        viewModelScope.launch {
            val studentId = sharedPrefManager.getStudent()!!.studentId
            // Fetch the unenrolled generated course IDs
            val unenrolledGeneratedCourseIdsResult = studentRepository.getUnenrolledGeneratedTextCourseIds(studentId)
            val unenrolledGeneratedCourseIds = unenrolledGeneratedCourseIdsResult.getOrNull()

            // Fetch the courses by IDs using your existing method
            if (unenrolledGeneratedCourseIds != null) {
                textCourseRepository.getTextCoursesByIds(unenrolledGeneratedCourseIds,
                    onSuccess = { unenrolledCreatedTextCourses ->
                        // Sort the courses by creationDate in descending order (newest first)
                        val sortedCourses = unenrolledCreatedTextCourses.sortedByDescending { it.creationDate }
                        Log.d("Courses", "Fetched and sorted unenrolled created text courses: $sortedCourses")
                        _generatedCourses.value = sortedCourses
                    },
                    onFailure = { e ->
                        Log.e("Courses", "Failed to fetch text courses", e)
                    }
                )
            }
        }
    }

    // A method to check if the flag in shared preferences is set
    fun checkForNewData() {
        val flag = sharedPrefManager.needsRefresh()
        _isNewDataAvailable.value = flag
    }

    // A method to clear the flag in shared preferences after fetching the data
    fun clearNewDataFlag() {
        sharedPrefManager.setNeedsRefresh(false)
    }

    fun decrementCourseGenerationTries(){
        sharedPrefManager.decrementCourseGenerationTries()
    }
}
