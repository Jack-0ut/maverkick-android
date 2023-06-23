package com.example.student.videolesson

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel Class for the 'El Bricko' Learning Assistant
 * It's responsible for storing the messages and chat-bot responses
 **/
class ChatViewModel : ViewModel() {
    // This list will hold chat messages
    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> get() = _messages

    /** Add new message to the dialog */
    fun addMessage(message: Message) {
        _messages.value?.add(message)
        _messages.value = _messages.value
    }
}
