package com.example.skillxchange

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private lateinit var messages: MutableList<ChatMessage>
    private lateinit var otherUserId: String
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val prefs = getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("user_id", "") ?: ""
        
        otherUserId = intent.getStringExtra("userId") ?: ""
        val userName = intent.getStringExtra("userName") ?: "Chat"
        val prefilledQuestion = intent.getStringExtra("prefilledQuestion")

        val toolbar = findViewById<MaterialToolbar>(R.id.chatToolbar)
        toolbar.title = userName
        toolbar.setNavigationOnClickListener { finish() }

        val rvMessages = findViewById<RecyclerView>(R.id.rvMessages)
        val etMessage = findViewById<EditText>(R.id.etChatMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSendMessage)

        messages = ChatCache.load(this, currentUserId, otherUserId)
        adapter = ChatAdapter(messages, currentUserId)

        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter

        // Mark as viewed when entering chat
        ChatCache.markViewed(this, currentUserId, otherUserId, true)

        // If there's a pre-filled question
        if (!prefilledQuestion.isNullOrEmpty()) {
            val newMessage = ChatMessage(prefilledQuestion, currentUserId)
            if (messages.none { it.text == prefilledQuestion && it.senderId == currentUserId }) {
                messages.add(newMessage)
                ChatCache.save(this, currentUserId, otherUserId, messages)
                adapter.notifyItemInserted(messages.size - 1)
                rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val newMessage = ChatMessage(text, currentUserId)
                messages.add(newMessage)
                ChatCache.save(this, currentUserId, otherUserId, messages)
                adapter.notifyItemInserted(messages.size - 1)
                rvMessages.scrollToPosition(messages.size - 1)
                etMessage.text.clear()
            }
        }
    }
}
