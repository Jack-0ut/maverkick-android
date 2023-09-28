package com.maverkick.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.POST
/**
 * LessonApi defines the REST API endpoints related to lesson creation and processing.
 * The interface should be used with a Retrofit instance to perform HTTP requests
 * for transcription and title generation for video lessons.
 */
interface LessonApi {

    /**
     * Transcribe the given video into text.
     *
     * @param request The request object containing the details needed to transcribe the video.
     *                This includes the course ID, lesson ID, file path, and language code.
     * @return A Response object containing the server's response. If the request is successful,
     *         the body of the response will contain the text transcription of the video.
     *
     * @see TranscriptionRequest for detailed information about the request object.
     */
    @POST("/transcribe")
    suspend fun transcribeVideo(
        @Body request: TranscriptionRequest
    ): Response<String>

    /**
     * Generate a title for the video lesson based on the given transcription context.
     *
     * @param context The transcription context, typically the text transcription of the video.
     * @return A Response object containing the server's response. If the request is successful,
     *         the body of the response will contain the generated title for the video lesson.
     */
    @POST("/generate-title")
    suspend fun generateLessonTitle(
        @Field("context") context: String
    ): Response<String>
}

/**
 * TranscriptionRequest represents the JSON body for the `/transcribe` endpoint.
 *
 * @property courseId The unique identifier for the course to which the lesson belongs.
 * @property lessonId The unique identifier for the lesson within the course.
 * @property filePath The path to the video file that needs to be transcribed.
 * @property languageCode The language code (e.g., 'en-US') representing the language of the video.
 *
 * This class is used to encapsulate all the required parameters to transcribe a video into text.
 */
data class TranscriptionRequest(
    val courseId: String,
    val lessonId: String,
    val filePath: String,
    val languageCode: String
)
