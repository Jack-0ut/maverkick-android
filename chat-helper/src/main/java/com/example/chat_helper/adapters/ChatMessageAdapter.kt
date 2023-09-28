package com.example.chat_helper.adapters

import android.text.method.ArrowKeyMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat_helper.R
import com.example.chat_helper.databinding.MessageItemBinding
import com.maverkick.data.models.AiChatMessage

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
        val newSize = newMessages.size
        messages.addAll(newMessages.subList(oldSize, newSize))

        notifyItemRangeInserted(oldSize, newSize - oldSize)
    }

}
