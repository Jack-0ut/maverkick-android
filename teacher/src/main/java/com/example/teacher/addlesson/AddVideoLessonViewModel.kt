package com.example.teacher.addlesson

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.data.repositories.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel that's responsible for the video selection and uploading
 **/
@HiltViewModel
class AddVideoLessonViewModel @Inject constructor(
    private val lessonRepository:LessonRepository
) : ViewModel() {

    private val _courseId = MutableStateFlow<String?>(null)
    val courseId: StateFlow<String?> = _courseId

    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress

    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.InProgress(0)) // Initial state
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus

    /** Store selected video **/
    fun selectVideo(uri: Uri) {
        _videoUri.value = uri
    }

    /** Store id of the course in which we will be adding new lesson **/
    fun setCourseId(courseId: String) {
        _courseId.value = courseId
    }

    /** Upload video with given uri and add it to the lesson document */
    fun uploadVideo() {
        val courseId = _courseId.value ?: return
        val videoUri = _videoUri.value ?: return
        // upload video to the storage
        Log.d("SexViewModel", "Attempting to upload video $videoUri to course $courseId")
        lessonRepository.uploadVideo(courseId, videoUri,
            onProgressListener = { progress ->
                _uploadProgress.value = progress
            },
            onSuccessListener = { (lessonId, downloadUrl) ->
                // Once the upload is done, we update the Firestore document
                lessonRepository.updateFirestoreWithVideoUrl(courseId, lessonId, downloadUrl,
                    onSuccessListener = {
                        _uploadStatus.value = UploadStatus.Success
                    },
                    onFailureListener = { exception ->
                        _uploadStatus.value = UploadStatus.Failure(exception)
                    }
                )
            },
            onFailureListener = { exception ->
                _uploadStatus.value = UploadStatus.Failure(exception)
            }
        )
    }
}


sealed class UploadStatus {
    object Success : UploadStatus()
    data class Failure(val exception: Exception) : UploadStatus()
    data class InProgress(val progress: Int) : UploadStatus()
}
