package com.maverkick.teacher.addlesson

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel that's responsible for the video selection and uploading
 **/
@HiltViewModel
class AddVideoLessonViewModel @Inject constructor() : ViewModel() {

    private val _courseId = MutableStateFlow<String?>(null)
    val courseId: StateFlow<String?> = _courseId

    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri

    private val _videoDuration = MutableStateFlow<Int?>(null)
    val videoDuration: StateFlow<Int?> = _videoDuration

    private val _language = MutableStateFlow<String?>(null)
    val language: StateFlow<String?> = _language


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

    fun setCourseLanguage(language:String) {
        _language.value = language
    }

    /** Stupid function that converts the name of the language into it's code */
    fun getBCP47LanguageTag(language: String?): String {
        return when (language) {
            "English US" -> "en-US"
            "English UK" -> "en-GB"
            "French" -> "fr-FR"
            "Spanish" -> "es-ES"
            "Ukrainian" -> "uk-UA"
            else -> "en-US"
        }
    }
}
