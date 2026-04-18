package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class ChatListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.chatsToolbar)
        val sharePostContent = intent.getStringExtra("sharePostContent")

        // Update title if we are sharing
        toolbar.title = if (sharePostContent != null) "Share with..." else "Messages"
        toolbar.setNavigationOnClickListener { finish() }

        val rvChats = findViewById<RecyclerView>(R.id.rvChats)

        // All users
        val allUsers = listOf(
            User("1", "Alex Thompson", "Kotlin Expert", "", listOf(), listOf()),
            User("2", "Sarah Jenkins", "UI Designer", "", listOf(), listOf()),
            User("3", "Michael Brown", "Public Speaker", "", listOf(), listOf()),
            User("4", "Emily Davis", "Data Science Expert", "", listOf(), listOf()),
            User("5", "Chris Wilson", "Video Editor", "", listOf(), listOf()),
            User("6", "Anna Smith", "Marketing Strategist", "", listOf(), listOf()),
            User("7", "John Lee", "Full Stack Developer", "", listOf(), listOf())
        )

        // If sharing, show all users so you can start a new chat to share. 
        // Otherwise, show only active chats.
        val displayUsers = if (sharePostContent != null) {
            allUsers
        } else {
            allUsers.filter { user -> ChatCache.load(this, user.id).isNotEmpty() }
        }

        rvChats.layoutManager = LinearLayoutManager(this)
        rvChats.adapter = ChatListAdapter(displayUsers) { user ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", user.id)
                putExtra("userName", user.name)
                putExtra("userTagline", user.tagline)
                if (sharePostContent != null) {
                    putExtra("prefilledQuestion", sharePostContent)
                }
            }
            startActivity(intent)
            if (sharePostContent != null) finish() // Close the picker after sharing
        }

        // Empty state handling
        val tvEmpty = findViewById<TextView>(R.id.tvNoChats)
        tvEmpty.visibility = if (displayUsers.isEmpty())
            android.view.View.VISIBLE
        else
            android.view.View.GONE
            
        if (sharePostContent != null && displayUsers.isEmpty()) {
            tvEmpty.text = "No users available to share with."
        }
    }
}