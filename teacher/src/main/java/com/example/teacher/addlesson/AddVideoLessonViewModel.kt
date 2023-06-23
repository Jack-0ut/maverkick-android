package com.example.teacher.addlesson

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that's responsible for the video selection and uploading
 **/
@HiltViewModel
class AddVideoLessonViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val _courseId = MutableStateFlow<String?>(null)
    val courseId: StateFlow<String?> = _courseId

    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri

    private val _videoDuration = MutableStateFlow<Int?>(null)
    val videoDuration: StateFlow<Int?> = _videoDuration

    private val _languageCode = MutableStateFlow<String?>(null)
    val languageCode: StateFlow<String?> = _languageCode

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress

    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.InProgress(0))
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus

    /** Store selected video **/
    fun selectVideo(uri: Uri) {
        _videoUri.value = uri
    }

    /** Store id of the course in which we will be adding new lesson **/
    fun setCourseId(courseId: String) {
        _courseId.value = courseId
    }

    fun setVideoDuration(duration: Int) {
        _videoDuration.value = duration
    }

    fun setCourseLanguageCode(languageCode:String) {
        _languageCode.value = languageCode
    }


    /** Upload video with given uri and add it to the lesson document */
}

/** Track the uploading of the video to the cloud storage **/
sealed class UploadStatus {
    object Success : UploadStatus()
    data class Failure(val exception: Exception) : UploadStatus()
    data class InProgress(val progress: Int) : UploadStatus()
}
