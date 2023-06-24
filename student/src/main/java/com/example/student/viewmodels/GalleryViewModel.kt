package com.example.student.viewmodels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.SearchCourseHit
import com.example.data.repositories.CourseRepository
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
    private val courseRepository: CourseRepository
) : ViewModel() {

    // Mutable LiveData for the list of search hits
    private val _searchHits = MutableLiveData<List<SearchCourseHit>?>()
    val searchHits: LiveData<List<SearchCourseHit>?> = _searchHits

    // MutableLiveData to hold the search query
    val searchQuery = MutableLiveData<String>()

    // In-memory cache for search results
    private var searchResultsCache: List<SearchCourseHit>? = null

    /** Search the courses for a given query **/
    fun searchCourses(query: String) {
        // Launch a coroutine in the ViewModel's scope
        viewModelScope.launch {
            // If the query is empty, load from cache
            if (query.isBlank() && searchResultsCache != null) {
                _searchHits.value = searchResultsCache
                return@launch
            }

            runCatching {
                // Fetch the list of search hits from the repository based on the query
                courseRepository.searchCourses(query)
            }.onSuccess { fetchedHits ->
                // Cache the fetched hits
                searchResultsCache = fetchedHits
                // Update the _searchHits LiveData with the fetched hits
                _searchHits.value = fetchedHits
            }.onFailure { exception ->
                // Handle the exception here
                Log.e(TAG, "Failed to fetch courses: ", exception)
            }
        }
    }
}
