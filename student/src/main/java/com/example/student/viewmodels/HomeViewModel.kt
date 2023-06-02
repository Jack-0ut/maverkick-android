package com.example.student.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.Lesson
import com.example.data.repositories.LessonRepository
import com.example.data.repositories.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the HomeFragment class
 * This ViewModel class would interact with our data source and
 * expose LiveData objects that Fragment can observe to update the UI.
 * So, basically it's fetching the list of lessons from the database and
 * store it, and it's gonna be used in the HomeFragment to display those lessons
 **/
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val studentRepository: StudentRepository
): ViewModel() {

    // LiveData object that the Fragment can observe to get the list of lessons
    private val _lessons = MutableLiveData<List<Lesson>>()
    val lessons: LiveData<List<Lesson>> get() = _lessons

    init {
        // getting the list of today's lessons
        //fetchLessons()
    }

    /** A coroutine method that fetch the list of lessons from the repository and assign it to the adapter*/
    private fun fetchLessons() {
        viewModelScope.launch {
            studentRepository.getCurrentStudent(
                onSuccess = { student ->
                    lessonRepository.getTodayLessons(student.studentId,
                        onSuccess = { lessonsList ->
                            _lessons.value = lessonsList
                        },
                        onFailure = { exception ->
                            // handle the exception, e.g., log it or display a message to the user
                        }
                    )
                },
                onFailure = { exception ->
                    // handle the exception, e.g., log it or display a message to the user
                }
            )
        }
    }
}

