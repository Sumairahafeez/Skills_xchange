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
    private var messages = mutableListOf<ChatMessage>()
    private lateinit var otherUserId: String
    private lateinit var currentUserId: String
    private lateinit var rvMessages: RecyclerView

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

        rvMessages = findViewById(R.id.rvMessages)
        val etMessage = findViewById<EditText>(R.id.etChatMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSendMessage)

        adapter = ChatAdapter(messages, currentUserId)
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = adapter

        // Listen for messages from Firebase
        ChatCache.listenToMessages(currentUserId, otherUserId) { newMessages ->
            messages.clear()
            messages.addAll(newMessages)
            adapter.notifyDataSetChanged()
            if (messages.isNotEmpty()) {
                rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        // Mark as viewed
        ChatCache.markViewed(this, currentUserId, otherUserId, true)

        rvMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                rvMessages.postDelayed({
                    if (messages.isNotEmpty()) {
                        rvMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }, 100)
            }
        }

        // If there's a pre-filled question, send it as the first message if chat is empty
        if (!prefilledQuestion.isNullOrEmpty()) {
            // We wait a bit to see if there are already messages
            rvMessages.postDelayed({
                if (messages.isEmpty()) {
                    val newMessage = ChatMessage(prefilledQuestion, currentUserId)
                    ChatCache.save(this, currentUserId, otherUserId, newMessage)
                }
            }, 1000)
        }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val newMessage = ChatMessage(text, currentUserId)
                ChatCache.save(this, currentUserId, otherUserId, newMessage)
                etMessage.text.clear()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        ChatCache.markViewed(this, currentUserId, otherUserId, true)
    }
}
