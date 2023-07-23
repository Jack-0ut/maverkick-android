package com.example.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** The interface that defines the endpoints we need for 'El Bricko' chat interaction */
interface ChatApi {
    /**
     * Start conversation and initialize it on the server
     * @param request StartConversationRequest contains:
     *  - user_id: ID of the user
     *  - course_id: ID of the course
     *  - lesson_id: ID of the lesson
     *  - context: Transcription of the video lesson
     * @return Response of type StartConversationResponse containing conversationID
     **/
    @POST("/start")
    suspend fun startConversation(
        @Body request: StartConversationRequest
    ): Response<StartConversationResponse>

    /**
     * Send message to the server and get an answer from 'El Bricko'
     * @param request SendMessageRequest contains:
     *  - user_id: ID of the user
     *  - course_id: ID of the course
     *  - lesson_id: ID of the lesson
     *  - message: Message text from the user
     * @return Response of type SendMessageResponse containing message from 'El Bricko'
     **/
    @POST("/chat")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
}


data class StartConversationRequest(
    @SerializedName("user_id")
    val userID: String,
    @SerializedName("course_id")
    val courseID: String,
    @SerializedName("lesson_id")
    val lessonID: String,
    @SerializedName("context")
    val context: String
)

data class StartConversationResponse(
    val conversationID: String
)

data class SendMessageRequest(
    @SerializedName("user_id")
    val userID: String,
    @SerializedName("course_id")
    val courseID: String,
    @SerializedName("lesson_id")
    val lessonID: String,
    @SerializedName("message")
    val message: String
)

data class SendMessageResponse(
    val message: String
)
