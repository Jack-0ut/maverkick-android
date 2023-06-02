package com.example.student.videolesson

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel Class for the Chat in the Video Player.
 * It's responsible for storing the messages
 **/
class ChatViewModel : ViewModel() {
    // This list will hold your chat messages
    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> get() = _messages

    fun addMessage(message: Message) {
        _messages.value?.add(message)
        _messages.value = _messages.value // Trigger the observer
    }
}
