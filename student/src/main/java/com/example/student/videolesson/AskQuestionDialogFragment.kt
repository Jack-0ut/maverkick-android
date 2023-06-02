package com.example.student.videolesson

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ArrowKeyMovementMethod
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.student.R
import com.example.student.databinding.FragmentAskQuestionDialogBinding
import com.example.student.databinding.MessageItemBinding


/**
 * Class that is responsible for displaying the chat
 * with AI-helper (For us ChatGPT)
 * It basically takes the student input related to the lesson
 * and tries to find an answer to that and return it to the
 * fragment as an answer
 * During dialog student could ask less than 5 questions
 **/
class AskQuestionDialogFragment : DialogFragment() {
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var binding: FragmentAskQuestionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAskQuestionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MessageAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.updateMessages(messages)
            binding.recyclerView.scrollToPosition(messages.size - 1)
        }

        // when student click on the send icon
        binding.inputLayout.setEndIconOnClickListener {
            val messageText = binding.promptInput.text.toString()
            if (messageText.isNotBlank()) {
                val message = Message(messageText, true)
                chatViewModel.addMessage(message)
                binding.promptInput.text?.clear()
                binding.inputLayout.isEnabled = false // Disable the input field

                // hide keyboard after student entered the necessary question prompt
                hideKeyboard()

                // TODO change how we would return the answer to the question
                Handler(Looper.getMainLooper()).postDelayed({
                    val botMessage = Message(
                        "Sure, here's an example of how to implement distributed data parallelism in TensorFlow using the `tf.distribute` module:\n",
                        false
                    )
                    chatViewModel.addMessage(botMessage)
                    binding.inputLayout.isEnabled = true // Enable the input field again
                }, 5000)
            }
        }
    }

    /** On dialog fragment creation we're setting the gravity,height and width for it*/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val width = (resources.displayMetrics.widthPixels * 0.98).toInt()
                val minHeight = (resources.displayMetrics.heightPixels * 0.9).toInt()
                window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                window?.setGravity(Gravity.CENTER)
                val params = window?.attributes
                if (params != null) {
                    params.height = minHeight
                    window?.attributes = params
                }
            }
        }
    }

    /** Method which responsible for displaying dialog perfectly even when we're changing orientation*/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Update dialog dimensions
        val width = (resources.displayMetrics.widthPixels * 0.98).toInt()
        val minHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
        dialog?.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        val params = dialog?.window?.attributes
        if (params != null) {
            params.height = minHeight
            dialog?.window?.attributes = params
        }
    }

    /** Method that will close the keyboard after student entered the question*/
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}

/**
 * Adapter for the Message class, which is responsible for
 * the displaying the chat with AI-powered bot, adding, updating
 * the UI in real time
 **/
class MessageAdapter(
    private val messages: MutableList<Message>
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

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

    fun updateMessages(newMessages: List<Message>) {
        val oldSize = messages.size
        messages.clear()
        messages.addAll(newMessages)
        val newSize = messages.size
        if (newSize > oldSize) {
            notifyItemInserted(newSize - 1)
        }
    }
}


data class Message(
    val text: String,
    val isUser: Boolean
)