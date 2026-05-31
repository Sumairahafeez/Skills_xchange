package com.example.skillxchange

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class MatchResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_result)

        val toolbar = findViewById<MaterialToolbar>(R.id.matchToolbar)
        val tvMatchTitle = findViewById<TextView>(R.id.tvMatchTitle)
        val rvMatches = findViewById<RecyclerView>(R.id.rvMatches)

        toolbar.setNavigationOnClickListener { finish() }

        val targetUserId = intent.getStringExtra("userId")
        val targetUserName = intent.getStringExtra("userName") ?: "someone"
        
        tvMatchTitle.text = "People matching with $targetUserName"

        val prefs = getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", "") ?: ""

        // Load users from UserCache which is now synced with Firebase
        UserCache.listenToUsers { allUsers ->
            val targetUser = allUsers.find { it.id == (targetUserId ?: currentUserId) }
            
            if (targetUser != null) {
                val matches = allUsers.filter { user ->
                    user.id != targetUser.id && (
                        user.teachSkills.any { it in targetUser.learnSkills } ||
                        user.learnSkills.any { it in targetUser.teachSkills }
                    )
                }

                val adapter = UserAdapter(matches.toMutableList()) { selectedUser ->
                    // Handle click if needed
                }
                rvMatches.layoutManager = LinearLayoutManager(this)
                rvMatches.adapter = adapter
            }
        }
    }
}
