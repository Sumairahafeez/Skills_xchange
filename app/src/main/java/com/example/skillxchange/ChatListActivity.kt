package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.Chat
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListActivity : AppCompatActivity() {

    private lateinit var adapter: ChatListAdapter
    private lateinit var currentUserId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvChats: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.chatsToolbar)
        toolbar.title = "Messages"
        toolbar.setNavigationOnClickListener { finish() }

        rvChats = findViewById(R.id.rvChats)
        tvEmpty = findViewById(R.id.tvNoChats)

        adapter = ChatListAdapter(emptyList(), currentUserId) { chat ->
            val otherUserId = chat.participants.find { it != currentUserId } ?: ""
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", otherUserId)
                putExtra("userName", chat.userNames[otherUserId])
            }
            startActivity(intent)
        }

        rvChats.layoutManager = LinearLayoutManager(this)
        rvChats.adapter = adapter

        listenForChats()
    }

    private fun listenForChats() {
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chats = snapshot.toObjects(Chat::class.java)
                    adapter.updateList(chats)
                    
                    if (chats.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "No messages yet. Start a conversation from the connections tab!"
                    } else {
                        tvEmpty.visibility = View.GONE
                    }
                }
            }
    }
}
