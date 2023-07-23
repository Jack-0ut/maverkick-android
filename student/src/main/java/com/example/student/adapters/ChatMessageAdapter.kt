package com.example.student.adapters

import android.text.method.ArrowKeyMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.models.AiChatMessage
import com.example.student.R
import com.example.student.databinding.MessageItemBinding

/**
 * Adapter for the AiChatMessage class, which is responsible for
 * the displaying the chat with AI-powered bot, adding, updating
 * UI in the real time
 **/
class ChatMessageAdapter(
    private val messages: MutableList<AiChatMessage>
) : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    class ViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MessageItemBinding.inflate(inflater, parent, false)
        binding.textMessage.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            isLongClickable = true
            movementMethod = ArrowKeyMovementMethod.getInstance()
            setTextIsSelectable(true)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]

        holder.binding.textMessage.text = message.text
        if (message.isUser) {
            holder.binding.textMessage.setBackgroundResource(R.drawable.background_user_message)
        } else {
            holder.binding.textMessage.setBackgroundResource(R.drawable.background_bot_message)

        }
    }
    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<AiChatMessage>) {
        val oldSize = messages.size
        messages.clear()
        messages.addAll(newMessages)
        val newSize = messages.size
        if (newSize > oldSize) {
            notifyItemInserted(newSize - 1)
        }
    }
}
