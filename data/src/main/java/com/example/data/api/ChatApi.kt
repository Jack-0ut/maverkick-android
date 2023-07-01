package com.example.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** The interface that defines the endpoints we need for 'El Bricko' chat interaction */
interface ChatApi {
    /** Start conversation and initialize it on the server **/
    @POST("/start")
    suspend fun startConversation(
        @Body request: StartConversationRequest
    ): Response<StartConversationResponse>

    /** Send message to the server and get an answer from 'El Bricko'**/
    @POST("/chat")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
}

data class StartConversationRequest(
    @SerializedName("user_id")
    val userID: String,
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
    @SerializedName("lesson_id")
    val lessonID: String,
    @SerializedName("message")
    val message: String
)

data class SendMessageResponse(
    val message: String
)
