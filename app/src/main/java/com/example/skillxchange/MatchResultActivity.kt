package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class MatchResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_result)

        val toolbar = findViewById<MaterialToolbar>(R.id.matchToolbar)
        val tvMatchTitle = findViewById<TextView>(R.id.tvMatchTitle)
        val rvMatches = findViewById<RecyclerView>(R.id.rvMatches)

        toolbar.setNavigationOnClickListener { finish() }

        val userName = intent.getStringExtra("userName") ?: "someone"
        tvMatchTitle.text = "People matching with $userName"

        val myLearnSkills = listOf("Kotlin", "Firebase", "Android")
        val myTeachSkills = listOf("UI Design", "Figma")
        val myTotalSkills = myLearnSkills + myTeachSkills

        // Mock data for matching
        val allUsers = listOf(
            User(uid = "1", name = "Ali Hassan", tagline = "Kotlin Dev", skills = listOf("Kotlin", "Android", "UI Design")),
            User(uid = "2", name = "Sara Khan", tagline = "UI Designer", skills = listOf("Figma", "UI Design", "Kotlin", "Firebase")),
            User(uid = "3", name = "Ahmed Raza", tagline = "Backend Dev", skills = listOf("Firebase", "Node.js", "Android")),
            User(uid = "4", name = "Zara Ahmed", tagline = "Data Analyst", skills = listOf("Python", "Excel", "Android")),
            User(uid = "5", name = "Usman Ali", tagline = "Speaker", skills = listOf("Communication", "Firebase"))
        )

        val matches = allUsers.filter { user ->
            user.skills.any { it in myTotalSkills }
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val adapter = UserAdapter(
            userList = matches,
            connectionInfo = emptyMap(),
            currentUserId = currentUserId,
            onConnectClick = { /* Handle in Explore */ },
            onAcceptClick = { /* Handle in Explore */ },
            onChatClick = { user ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", user.uid)
                    putExtra("userName", user.name)
                }
                startActivity(intent)
            },
            onProfileClick = { user ->
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userId", user.uid)
                }
                startActivity(intent)
            }
        )
        rvMatches.layoutManager = LinearLayoutManager(this)
        rvMatches.adapter = adapter
    }
}
