package com.example.student.videolesson

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ArrowKeyMovementMethod
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.student.R
import com.example.student.databinding.FragmentAskQuestionDialogBinding
import com.example.student.databinding.MessageItemBinding
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import dagger.hilt.android.AndroidEntryPoint

/**
 * Class that is responsible for displaying the chat with AI-helper.
 * It basically takes the student input related to the lesson and tries to find an answer to that
 **/
@AndroidEntryPoint
class AskQuestionDialogFragment : DialogFragment() {
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var binding: FragmentAskQuestionDialogBinding

    // Parameters
    private lateinit var lessonId:String
    private lateinit var transcription:String
    private val maxRequests = 5
    private var progressBarGIFDialogBuilder: ProgressBarGIFDialog.Builder? = null

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

        lessonId = arguments?.getString("lessonId") ?: ""
        transcription = arguments?.getString("transcription") ?: ""

        val adapter = MessageAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.updateMessages(messages)
            binding.recyclerView.scrollToPosition(messages.size - 1)
        }

        // display the dialog, while we're getting response from the server
        chatViewModel.isMessageGenerationInProgress.observe(viewLifecycleOwner) { inProgress ->
            if (inProgress) {
                progressBarGIFDialogBuilder = ProgressBarGIFDialog.Builder(requireActivity())
                    .setCancelable(false)
                    .setTitleColor(com.example.common.R.color.black) // Set Title Color (int only)
                    .setLoadingGif(R.drawable.progress)
                    .setDoneTitle("Here it is!") // Set Done Title
                    .setLoadingTitle("Wait, I'm working on answer...") // Set Loading Title

                progressBarGIFDialogBuilder?.build()
            } else {
                progressBarGIFDialogBuilder?.clear() // Clear the dialog when inProgress is false
                progressBarGIFDialogBuilder = null
            }
        }

        // Start the conversation when the dialog is first displayed
        chatViewModel.startConversation(lessonId,transcription)

        // observe the request counter
        chatViewModel.requestCount.observe(viewLifecycleOwner) { count ->
            // update counter
            binding.counter.text = "${maxRequests - count}"

            // adjust the alpha of the icon view
            val remainingRequests = maxRequests - count
            val alphaValue = remainingRequests.toFloat() / maxRequests
            binding.energyIcon.alpha = alphaValue

            // apply the fade in animation
            val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            binding.counter.startAnimation(fadeInAnimation)
            binding.energyIcon.startAnimation(fadeInAnimation)
        }

        // when student click on the send icon
        binding.inputLayout.setEndIconOnClickListener {
            val messageText = binding.promptInput.text.toString()
            if (messageText.isNotBlank() && chatViewModel.requestCount.value!! < maxRequests) {
                val message = Message(messageText, true)
                chatViewModel.addMessage(message)
                binding.promptInput.text?.clear()

                hideKeyboard()

                chatViewModel.sendMessage(lessonId, messageText)
                chatViewModel.incrementRequestCount()

            } else if (chatViewModel.requestCount.value!! >= maxRequests) {
                Toast.makeText(context, "Maximum number of requests reached", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Method that will close the keyboard after student entered the question*/
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    /** On dialog fragment creation we're setting the gravity,height and width for it*/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val width = (resources.displayMetrics.widthPixels)
                val height = (resources.displayMetrics.heightPixels)
                window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                window?.setGravity(Gravity.CENTER)
                val params = window?.attributes
                if (params != null) {
                    params.height = height
                    window?.attributes = params
                }
            }
        }
    }

    /** Method which responsible for displaying dialog perfectly even when we're changing orientation*/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Update dialog dimensions
        val width = (resources.displayMetrics.widthPixels)
        val height = (resources.displayMetrics.heightPixels)
        dialog?.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        val params = dialog?.window?.attributes
        if (params != null) {
            params.height = height
            dialog?.window?.attributes = params
        }
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