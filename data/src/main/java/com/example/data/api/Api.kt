package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.POST

/** The interface that defines the endpoints we need in our app */
interface Api {
    /** Transcribe the video into text **/
    @POST("/transcribe")
    suspend fun transcribeVideo(
        @Body request: TranscriptionRequest
    ): Response<String>

    /** Use the transcription to generate the title for the videolesson **/
    @POST("/generate-title")
    suspend fun generateLessonTitle(
        @Field("context") context: String
    ):Response<String>
}

/** Class which represent the body for the /transcribe endpoint**/
data class TranscriptionRequest(
    val courseId: String,
    val lessonId: String,
    val filePath: String,
    val languageCode: String
)
