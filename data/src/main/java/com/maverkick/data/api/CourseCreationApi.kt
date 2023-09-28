package com.maverkick.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * CourseCreationApi defines the REST API endpoints related to course creation.
 * The interface should be used with a Retrofit instance to perform HTTP requests.
 */
interface CourseCreationApi {

    /**
     * Generate a course based on the given prompts.
     *
     * @param request The request object containing the details needed to generate a course.
     *                This includes the user ID, course prompt, language, and desired number of lessons.
     * @return A Response object containing the server's response. If the request is successful, the
     *         body of the response will contain a string representation of the generated course.
     *
     * @see CourseGenerationRequest for detailed information about the request object.
     */
    @POST("/generate-course")
    suspend fun generateCourse(
        @Body request: CourseGenerationRequest
    ): Response<CourseGenerationResponse>
}

/**
 * CourseGenerationRequest represents the JSON body for the `/generate-course` endpoint.
 *
 * @property userId The unique identifier for the user requesting the course generation.
 * @property coursePrompt The textual description or prompt used to guide the generation of the course.
 * @property language The language in which the course is to be generated.
 *
 * The property names are annotated with @SerializedName to map the Kotlin property names to the
 * corresponding JSON field names. This ensures that the serialization and deserialization processes
 * will function correctly even if the property names in the code and the JSON do not match exactly.
 */
data class CourseGenerationRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("prompt") val coursePrompt: String,
    @SerializedName("language") val language: String,
)

data class CourseGenerationResponse(
    @SerializedName("courseId") val courseId: String
)