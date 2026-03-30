package com.example.skillsexchange

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

        val userName = intent.getStringExtra("userName") ?: "someone"
        tvMatchTitle.text = "People matching with $userName"

        val myLearnSkills = listOf("Kotlin", "Firebase", "Android")
        val myTeachSkills = listOf("UI Design", "Figma")

        val allUsers = mutableListOf(
            User("1", "Ali Hassan", "Kotlin Dev", "", listOf("Kotlin", "Android"), listOf("UI Design")),
            User("2", "Sara Khan", "UI Designer", "", listOf("Figma", "UI Design"), listOf("Kotlin", "Firebase")),
            User("3", "Ahmed Raza", "Backend Dev", "", listOf("Firebase", "Node.js"), listOf("Android")),
            User("4", "Zara Ahmed", "Data Analyst", "", listOf("Python", "Excel"), listOf("Android")),
            User("5", "Usman Ali", "Speaker", "", listOf("Communication"), listOf("Firebase"))
        )

        val matches = allUsers.filter { user ->
            user.teachSkills.any { it in myLearnSkills } ||
                    user.learnSkills.any { it in myTeachSkills }
        }

        val adapter = UserAdapter(matches.toMutableList()) { }
        rvMatches.layoutManager = LinearLayoutManager(this)
        rvMatches.adapter = adapter
    }
}