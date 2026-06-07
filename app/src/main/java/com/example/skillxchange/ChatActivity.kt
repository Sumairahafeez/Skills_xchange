package com.example.skillxchange

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.Message
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var otherUserId: String
    private lateinit var currentUserId: String
    private lateinit var chatId: String
    private lateinit var rvMessages: RecyclerView
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var currentUser: User? = null
    private var otherUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""
        
        otherUserId = intent.getStringExtra("userId") ?: ""
        val userName = intent.getStringExtra("userName") ?: "Chat"
        
        // Generate consistent chatId: smaller UID first
        chatId = if (currentUserId < otherUserId) "${currentUserId}_${otherUserId}" else "${otherUserId}_${currentUserId}"

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

        fetchUsers()
        listenForMessages()

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }
        
        val prefilled = intent.getStringExtra("prefilledQuestion")
        if (!prefilled.isNullOrEmpty()) {
            etMessage.setText(prefilled)
        }
    }

    private fun fetchUsers() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener {
            currentUser = it.toObject(User::class.java)
        }
        db.collection("users").document(otherUserId).get().addOnSuccessListener {
            otherUser = it.toObject(User::class.java)
        }
    }

    private fun listenForMessages() {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    messages.clear()
                    messages.addAll(snapshot.toObjects(Message::class.java))
                    adapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }
    }

    private fun sendMessage(text: String) {
        val messageId = UUID.randomUUID().toString()
        val message = Message(
            id = messageId,
            senderId = currentUserId,
            text = text,
            timestamp = Timestamp.now() // Local timestamp for immediate feedback
        )

        val chatRef = db.collection("chats").document(chatId)
        
        db.runBatch { batch ->
            batch.set(chatRef.collection("messages").document(messageId), message)
            
            val chatData = mutableMapOf<String, Any>(
                "id" to chatId,
                "participants" to listOf(currentUserId, otherUserId),
                "lastMessage" to text,
                "lastSenderId" to currentUserId,
                "lastTimestamp" to FieldValue.serverTimestamp()
            )

            val userNames = mutableMapOf<String, String>()
            currentUser?.let { userNames[currentUserId] = it.name }
            otherUser?.let { userNames[otherUserId] = it.name ?: intent.getStringExtra("userName") ?: "" }
            if (userNames.isNotEmpty()) chatData["userNames"] = userNames

            val userPhotos = mutableMapOf<String, String>()
            currentUser?.let { userPhotos[currentUserId] = it.photoUrl }
            otherUser?.let { userPhotos[otherUserId] = it.photoUrl }
            if (userPhotos.isNotEmpty()) chatData["userPhotos"] = userPhotos

            batch.set(chatRef, chatData, SetOptions.merge())
        }.addOnSuccessListener {
            // Notify other user
            NotificationHelper.createNotification(
                toUserId = otherUserId,
                fromUserId = currentUserId,
                fromUserName = currentUser?.name ?: auth.currentUser?.displayName ?: "Someone",
                fromUserProfileUrl = currentUser?.photoUrl ?: "",
                message = text,
                type = "MESSAGE",
                relatedId = chatId
            )
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
        }
    }
}
