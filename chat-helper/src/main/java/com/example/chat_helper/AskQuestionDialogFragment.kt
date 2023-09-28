package com.example.chat_helper

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat_helper.databinding.FragmentAskQuestionDialogBinding
import com.google.android.material.snackbar.Snackbar
import com.maverkick.data.models.AiChatMessage
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

    private lateinit var courseId: String
    private lateinit var lessonId: String
    private lateinit var transcription: String
    private val maxRequests = 5
    private var progressBarGIFDialogBuilder: ProgressBarGIFDialog.Builder? = null

    var dismissListener: (() -> Unit)? = null

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
        initVariablesFromArguments()
        initRecyclerView()
        observeViewModel()
        startConversation()
        handleSendMessage()
    }

    private fun initVariablesFromArguments() {
        arguments?.let {
            courseId = it.getString("courseId", "")
            lessonId = it.getString("lessonId", "")
            transcription = it.getString("transcription", "")
            if (courseId.isBlank() || lessonId.isBlank() || transcription.isBlank()) {
                showSnackbar("Missing argument data!")
                dismiss()
            }
        } ?: run {
            showSnackbar("No arguments found!")
            dismiss()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun initRecyclerView() {
        val adapter = com.example.chat_helper.adapters.ChatMessageAdapter(mutableListOf())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun observeViewModel() {
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            (binding.recyclerView.adapter as com.example.chat_helper.adapters.ChatMessageAdapter).updateMessages(messages)
            binding.recyclerView.scrollToPosition(messages.size - 1)
        }

        observeMessageGeneration()
        observeRequestCount()
    }

    private fun observeMessageGeneration() {
        chatViewModel.isMessageGenerationInProgress.observe(viewLifecycleOwner) { inProgress ->
            handleProgress(inProgress)
        }
    }

    private fun handleProgress(inProgress: Boolean) {
        if (inProgress) {
            showProgress()
        } else {
            hideProgress()
        }
    }

    private fun showProgress() {
        progressBarGIFDialogBuilder = ProgressBarGIFDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitleColor(com.maverkick.common.R.color.black)
            .setLoadingGif(com.maverkick.common.R.drawable.progress)
            .setDoneTitle("Here it is!") // Set Done Title
            .setLoadingTitle("Wait, I'm working on answer...")

        progressBarGIFDialogBuilder?.build()
    }

    private fun hideProgress() {
        progressBarGIFDialogBuilder?.clear()
        progressBarGIFDialogBuilder = null
    }

    private fun startConversation() {
        chatViewModel.startConversation(courseId, lessonId, transcription)
    }

    private fun observeRequestCount() {
        chatViewModel.requestCount.observe(viewLifecycleOwner) { count ->
            handleRequestCount(count)
        }
    }

    private fun handleRequestCount(count: Int) {
        updateCounter(count)
        adjustIconAlpha(count)
        applyFadeInAnimation()
    }

    private fun updateCounter(count: Int) {
        binding.counter.text = "${maxRequests - count}"
    }

    private fun adjustIconAlpha(count: Int) {
        val remainingRequests = maxRequests - count
        val alphaValue = remainingRequests.toFloat() / maxRequests
        binding.energyIcon.alpha = alphaValue
    }

    private fun applyFadeInAnimation() {
        val fadeInAnimation = AnimationUtils.loadAnimation(context, com.maverkick.common.R.anim.fade_in)
        binding.counter.startAnimation(fadeInAnimation)
        binding.energyIcon.startAnimation(fadeInAnimation)
    }

    private fun handleSendMessage() {
        binding.inputLayout.setEndIconOnClickListener {
            val messageText = binding.promptInput.text.toString()
            if (messageText.isNotBlank() && chatViewModel.requestCount.value!! < maxRequests) {
                processUserMessage(messageText)
            } else if (chatViewModel.requestCount.value!! >= maxRequests) {
                Toast.makeText(context, "Maximum number of requests reached", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processUserMessage(messageText: String) {
        val message = AiChatMessage(messageText, true)
        chatViewModel.addMessage(message)
        binding.promptInput.text?.clear()

        hideKeyboard()

        chatViewModel.sendMessage(courseId, lessonId, messageText)
        chatViewModel.incrementRequestCount()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, com.maverkick.common.R.style.FullScreenDialogStyle)
    }

    /** On dialog fragment creation we're setting the gravity,height and width for it*/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                window?.setGravity(Gravity.CENTER)
            }
        }
    }

    /** Method which responsible for displaying dialog perfectly even when we're changing orientation*/
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
    }
}
