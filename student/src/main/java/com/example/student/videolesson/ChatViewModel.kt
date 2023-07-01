package com.example.student.videolesson

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ChatApi
import com.example.data.api.SendMessageRequest
import com.example.data.api.StartConversationRequest
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel Class for the 'El Bricko' Learning Assistant
 * It's responsible for storing the messages and chat-bot responses
 **/
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager,
    private val chatApi: ChatApi,
) : ViewModel()
{
    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> get() = _messages

    private val _isMessageGenerationInProgress = MutableLiveData(false)
    val isMessageGenerationInProgress: LiveData<Boolean> get() = _isMessageGenerationInProgress

    private val _requestCount = MutableLiveData(0)
    val requestCount: LiveData<Int> get() = _requestCount

    fun incrementRequestCount() {
        val updatedCount = _requestCount.value?.plus(1) ?: 0
        _requestCount.postValue(updatedCount)
    }

    fun resetRequestCount() {
        _requestCount.postValue(0)
    }

    fun addMessage(message: Message) {
        _messages.value?.apply {
            add(message)
            _messages.postValue(this)
        }
    }

    /** Communicate with api to start the conversation **/
    fun startConversation(lessonId: String, context: String) {
        val studentId = sharedPrefManager.getStudent()!!.studentId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startConversationResponse = chatApi.startConversation(StartConversationRequest(
                    sharedPrefManager.getStudent()!!.studentId, lessonId, context))
                if (startConversationResponse.isSuccessful) {
                } else {
                    addMessage(Message("Sorry, can't start the conversation", false))
                }
            } catch (e: Exception) {
            }
        }
    }


    /** Communicate with api to send the message and return an answer **/
    fun sendMessage(lessonId: String, messageText: String) {
        _isMessageGenerationInProgress.postValue(true) // Indicate that a message is being sent and processed
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sendMessageResponse = chatApi.sendMessage(SendMessageRequest(sharedPrefManager.getStudent()!!.studentId, lessonId, messageText))
                if (sendMessageResponse.isSuccessful) {
                    val responseMessage = sendMessageResponse.body()?.message
                    if (!responseMessage.isNullOrBlank()) {
                        // Switch to the main thread to update the UI
                        withContext(Dispatchers.Main) {
                            val botMessage = Message(responseMessage, false)
                            addMessage(botMessage)
                        }
                    }
                } else {
                    addMessage(Message("Sorry, can't give you an answer",false))
                }
            } catch (e: Exception) {
            } finally {
                _isMessageGenerationInProgress.postValue(false) // Indicate that the message has been processed
            }
        }
    }
}
