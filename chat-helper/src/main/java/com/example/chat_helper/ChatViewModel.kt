package com.example.chat_helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maverkick.data.api.ChatApi
import com.maverkick.data.api.SendMessageRequest
import com.maverkick.data.api.StartConversationRequest
import com.maverkick.data.models.AiChatMessage
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

/**
 * ViewModel Class for the 'El Bricko' Learning Assistant
 * It's responsible for storing the messages and chat-bot responses
 **/
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager,
    private val chatApi: ChatApi,
) : ViewModel() {

    private val _messages = MutableLiveData<MutableList<AiChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<AiChatMessage>> get() = _messages

    private val _isMessageGenerationInProgress = MutableLiveData(false)
    val isMessageGenerationInProgress: LiveData<Boolean> get() = _isMessageGenerationInProgress

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _requestCount = MutableLiveData(0)
    val requestCount: LiveData<Int> get() = _requestCount

    fun incrementRequestCount() {
        _requestCount.value = _requestCount.value?.plus(1)
    }

    fun addMessage(message: AiChatMessage) {
        val updatedMessages = _messages.value?.toMutableList() ?: mutableListOf()
        updatedMessages.add(message)
        _messages.postValue(updatedMessages)
    }

    fun startConversation(courseId: String, lessonId: String, context: String) {
        sharedPrefManager.getStudent()?.studentId?.let {
            handleNetworkRequest(
                request = { chatApi.startConversation(StartConversationRequest(it, courseId, lessonId, context)) },
                onError = { _error.postValue("Sorry, can't start the conversation") }
            )
        } ?: run { _error.postValue("User is not logged in.") }
    }

    fun sendMessage(courseId: String, lessonId: String, messageText: String) {
        val studentId = sharedPrefManager.getStudent()?.studentId
        _isMessageGenerationInProgress.postValue(true)
        studentId?.let {
            handleNetworkRequest(
                request = { chatApi.sendMessage(SendMessageRequest(it, courseId, lessonId, messageText)) },
                onSuccess = { response ->
                    response.body()?.message?.let { responseMessage ->
                        if (responseMessage.isNotBlank()) {
                            val botMessage = AiChatMessage(responseMessage, false)
                            addMessage(botMessage)
                        }
                    }
                },
                onError = { _error.postValue("Sorry, can't give you an answer") },
                onComplete = { _isMessageGenerationInProgress.postValue(false) }
            )
        } ?: run { _error.postValue("User is not logged in.") }
    }

    private fun <T> handleNetworkRequest(
        request: suspend () -> Response<T>,
        onSuccess: suspend (Response<T>) -> Unit = {},
        onError: () -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = request()
                if (response.isSuccessful) {
                    onSuccess(response)
                } else {
                    onError()
                }
            } catch (e: Exception) {
                _error.postValue("Network Request Error: ${e.message}")
                onError()
            } finally {
                onComplete()
            }
        }
    }
}

