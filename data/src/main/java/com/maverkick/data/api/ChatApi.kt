package com.maverkick.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * ChatApi defines the REST API endpoints for interacting with 'El Bricko' chat.
 * This interface should be used with a Retrofit instance to initiate and manage
 * chat conversations.
 */
interface ChatApi {

    /**
     * Starts a conversation with 'El Bricko' and initializes it on the server.
     *
     * @param request The request object containing details needed to start the conversation,
     *                such as user ID, course ID, lesson ID, and video lesson transcription.
     * @return A Response object containing the conversation ID, needed for further interaction.
     * @see StartConversationRequest for detailed information about the request object.
     */
    @POST("/start")
    suspend fun startConversation(
        @Body request: StartConversationRequest
    ): Response<StartConversationResponse>

    /**
     * Sends a message to the server to get an answer from 'El Bricko'.
     *
     * @param request The request object containing details needed for the chat interaction,
     *                such as user ID, course ID, lesson ID, and user's message text.
     * @return A Response object containing the message from 'El Bricko'.
     * @see SendMessageRequest for detailed information about the request object.
     */
    @POST("/chat")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
}

/**
 * StartConversationRequest represents the JSON body for the `/start` endpoint.
 * It encapsulates the parameters needed to initiate a conversation with 'El Bricko'.
 *
 * @property userID The unique identifier for the user.
 * @property courseID The unique identifier for the course.
 * @property lessonID The unique identifier for the lesson.
 * @property context The transcription of the video lesson.
 */
data class StartConversationRequest(
    @SerializedName("user_id") val userID: String,
    @SerializedName("course_id") val courseID: String,
    @SerializedName("lesson_id") val lessonID: String,
    @SerializedName("context") val context: String
)

/**
 * StartConversationResponse represents the server's response to starting a conversation.
 *
 * @property conversationID The unique identifier for the initiated conversation.
 */
data class StartConversationResponse(
    val conversationID: String
)

/**
 * SendMessageRequest represents the JSON body for the `/chat` endpoint.
 * It encapsulates the parameters needed to send a message to 'El Bricko'.
 *
 * @property userID The unique identifier for the user.
 * @property courseID The unique identifier for the course.
 * @property lessonID The unique identifier for the lesson.
 * @property message The text of the message sent by the user.
 */
data class SendMessageRequest(
    @SerializedName("user_id") val userID: String,
    @SerializedName("course_id") val courseID: String,
    @SerializedName("lesson_id") val lessonID: String,
    @SerializedName("message") val message: String
)

/**
 * SendMessageResponse represents the server's response to sending a message to 'El Bricko'.
 *
 * @property message The text of the message received from 'El Bricko'.
 */
data class SendMessageResponse(
    val message: String
)
