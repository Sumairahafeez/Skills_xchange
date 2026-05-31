package com.example.skillxchange

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class ChatListActivity : AppCompatActivity() {

    private lateinit var adapter: ChatListAdapter
    private lateinit var currentUserId: String
    private var allUsers = listOf<User>()
    private var friendsIds = setOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.chatsToolbar)
        val sharePostContent = intent.getStringExtra("sharePostContent")

        toolbar.title = if (sharePostContent != null) "Share with..." else "Messages"
        toolbar.setNavigationOnClickListener { finish() }

        val rvChats = findViewById<RecyclerView>(R.id.rvChats)

        val prefs = getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("user_id", "") ?: ""
        
        adapter = ChatListAdapter(emptyList()) { user ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", user.id)
                putExtra("userName", user.name)
                putExtra("userTagline", user.tagline)
                if (sharePostContent != null) {
                    putExtra("prefilledQuestion", sharePostContent)
                }
            }
            startActivity(intent)
            if (sharePostContent != null) finish()
        }

        rvChats.layoutManager = LinearLayoutManager(this)
        rvChats.adapter = adapter
        
        setupFirebaseListeners()
    }

    private fun setupFirebaseListeners() {
        val tvEmpty = findViewById<TextView>(R.id.tvNoChats)
        
        UserCache.listenToUsers { users ->
            allUsers = users
            ConnectionCache.listenToFriends(currentUserId) { friends ->
                friendsIds = friends
                val chatUsers = allUsers.filter { friendsIds.contains(it.id) }
                
                adapter.updateList(chatUsers)
                
                if (chatUsers.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "No messages yet. Accept an invitation to start chatting!"
                } else {
                    tvEmpty.visibility = View.GONE
                }
            }
        }
    }
}
