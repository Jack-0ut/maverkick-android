package com.example.data.models

/**
 * Represents a message in the AI chat.
 *
 * @property text The content of the chat message.
 * @property isUser Indicates whether the message was sent by the user or not. If true, the message was sent by the user.
 *                 If false, the message was sent by the AI.
 */
data class AiChatMessage(
    val text: String,
    val isUser: Boolean
)
