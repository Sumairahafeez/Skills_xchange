package com.example.skillsexchange

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messages: MutableList<ChatMessage>

    private lateinit var otherUserId: String
    private lateinit var otherUserName: String
    private lateinit var otherUserTagline: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        otherUserId      = intent.getStringExtra("userId") ?: "unknown"
        otherUserName    = intent.getStringExtra("userName") ?: "User"
        otherUserTagline = intent.getStringExtra("userTagline") ?: "Skills Exchange Member"

        val prefilledQuestion = intent.getStringExtra("prefilledQuestion")
        val sharePostId = intent.getStringExtra("sharePostId")

        val toolbar = findViewById<MaterialToolbar>(R.id.chatToolbar)
        toolbar.title = otherUserName
        toolbar.subtitle = otherUserTagline
        toolbar.setNavigationOnClickListener { finish() }

        rvChat    = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend   = findViewById(R.id.btnSend)

        messages = ChatCache.load(this, otherUserId)
        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = chatAdapter

        // If sharing a specific post
        if (sharePostId != null && prefilledQuestion != null) {
            sendMessage(prefilledQuestion, sharePostId)
        } else if (!prefilledQuestion.isNullOrBlank() && messages.isEmpty()) {
            sendMessage(prefilledQuestion)
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                etMessage.setText("")
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(text: String, sharedPostId: String? = null) {
        val userMsg = ChatMessage(text = text, isSentByMe = true, sharedPostId = sharedPostId)
        chatAdapter.addMessage(userMsg)
        rvChat.scrollToPosition(messages.size - 1)
        saveCache()
        if (sharedPostId == null) {
            getAiReply(text)
        }
    }

    private fun getAiReply(userMessage: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val reply = generateSmartReply(userMessage, otherUserName, otherUserTagline)
            val replyMsg = ChatMessage(text = reply, isSentByMe = false)
            chatAdapter.addMessage(replyMsg)
            rvChat.scrollToPosition(messages.size - 1)
            saveCache()
        }, 1200)
    }

    private fun generateSmartReply(message: String, name: String, tagline: String): String {
        val msg = message.lowercase()

        return when {
            msg.contains("teach") || msg.contains("learn") ->
                "I'd love to help! Teaching is something I'm really passionate about. When would you like to start?"

            msg.contains("kotlin") || msg.contains("android") ->
                "Kotlin is such a great language! I've been working with it for a while now. What specific part are you struggling with?"

            msg.contains("figma") || msg.contains("design") || msg.contains("ui") ->
                "Design is all about iteration! I can walk you through my workflow step by step. What's your current skill level?"

            msg.contains("firebase") || msg.contains("backend") ->
                "Firebase is actually easier than it looks once you get the hang of it. Happy to share some resources!"

            msg.contains("python") || msg.contains("data") ->
                "Python is super versatile! Are you looking into data science specifically or just general programming?"

            msg.contains("hello") || msg.contains("hi") || msg.contains("hey") ->
                "Hey! 👋 Great to connect with you on Skills Xchange. What can I help you with?"

            msg.contains("help") || msg.contains("question") ->
                "Of course, happy to help! Go ahead and ask, I'll do my best to answer."

            msg.contains("meet") || msg.contains("call") || msg.contains("session") ->
                "Sure, a session sounds great! I'm usually free on weekends. Does that work for you?"

            msg.contains("thanks") || msg.contains("thank you") ->
                "Anytime! That's what Skills Xchange is all about 😊"

            msg.contains("how") ->
                "Good question! It depends on a few things — tell me more about what you're trying to achieve."

            msg.contains("project") || msg.contains("work") ->
                "Oh interesting! I'd love to hear more about your project. What stack are you using?"

            msg.contains("exchange") || msg.contains("swap") || msg.contains("trade") ->
                "A skill swap sounds awesome! I can teach you $tagline stuff and you can help me with something in return 🤝"

            else ->
                listOf(
                    "That's really interesting! Tell me more.",
                    "I completely agree with that perspective!",
                    "Great point! I've been thinking about that too.",
                    "Sounds good to me. Let's figure this out together.",
                    "I appreciate you reaching out. Let's connect properly!"
                ).random()
        }
    }

    private fun saveCache() {
        ChatCache.save(this, otherUserId, messages)
    }
}