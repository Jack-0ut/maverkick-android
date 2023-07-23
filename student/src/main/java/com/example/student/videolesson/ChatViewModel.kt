package com.example.student.videolesson

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ChatApi
import com.example.data.api.SendMessageRequest
import com.example.data.api.StartConversationRequest
import com.example.data.models.AiChatMessage
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
) : ViewModel()
{
    private val _messages = MutableLiveData<MutableList<AiChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<AiChatMessage>> get() = _messages

    private val _isMessageGenerationInProgress = MutableLiveData(false)
    val isMessageGenerationInProgress: LiveData<Boolean> get() = _isMessageGenerationInProgress

    private val _requestCount = MutableLiveData(0)
    val requestCount: LiveData<Int> get() = _requestCount

    fun incrementRequestCount() {
        val updatedCount = _requestCount.value?.plus(1) ?: 0
        _requestCount.postValue(updatedCount)
    }

    fun addMessage(message: AiChatMessage) {
        _messages.value?.apply {
            add(message)
            _messages.postValue(this)
        }
    }

    /** Communicate with api to start the conversation **/
    fun startConversation(courseId: String, lessonId: String, context: String) {
        val studentId = sharedPrefManager.getStudent()?.studentId
        studentId?.let {
            handleNetworkRequest(
                request = { chatApi.startConversation(StartConversationRequest(it, courseId, lessonId, context)) },
                onError = { addMessage(AiChatMessage("Sorry, can't start the conversation", false)) }
            )
        } ?: run { addMessage(AiChatMessage("User is not logged in.", false)) }
    }

    /** Communicate with api to send the message and return an answer **/
    fun sendMessage(courseId: String, lessonId: String, messageText: String) {
        val studentId = sharedPrefManager.getStudent()?.studentId
        _isMessageGenerationInProgress.postValue(true) // Indicate that a message is being sent and processed
        studentId?.let {
            handleNetworkRequest(
                request = { chatApi.sendMessage(SendMessageRequest(it, courseId, lessonId, messageText)) },
                onSuccess = { response ->
                    val responseMessage = response.body()?.message
                    if (!responseMessage.isNullOrBlank()) {
                        // Switch to the main thread to update the UI
                        withContext(Dispatchers.Main) {
                            val botMessage = AiChatMessage(responseMessage, false)
                            addMessage(botMessage)
                        }
                    }
                },
                onError = { addMessage(AiChatMessage("Sorry, can't give you an answer", false)) },
                onComplete = { _isMessageGenerationInProgress.postValue(false) }
            )
        } ?: run { addMessage(AiChatMessage("User is not logged in.", false)) }
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
                Log.e("NetworkRequestError", "Exception", e)
                onError()
            } finally {
                onComplete()
            }
        }
    }
}

