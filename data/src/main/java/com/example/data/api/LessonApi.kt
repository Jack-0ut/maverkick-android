package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.POST

/** The interface that defines the endpoints we need for lesson creation */
interface LessonApi {
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

