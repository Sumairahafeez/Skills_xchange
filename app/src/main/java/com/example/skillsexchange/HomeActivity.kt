package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Adjust UI for Feed
        findViewById<TextView>(R.id.tvWelcome).text = "Skill Feed"
        findViewById<View>(R.id.filterLayout).visibility = View.GONE

        rvFeed = findViewById(R.id.rvUsers) // Reusing the same ID for simplicity
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)

        tvProfileName.text = "Osama"

        val posts = listOf(
            Post("1", "Alex Thompson", "Kotlin Expert", "Just finished a tutorial on Coroutines! Check it out.", "2h ago", 15, 3, false),
            Post("2", "Sarah Jenkins", "UI Designer", "New Figma shortcuts that will save you hours.", "5h ago", 42, 10, true),
            Post("3", "Michael Brown", "Public Speaker", "How to overcome stage fright in 5 steps.", "1d ago", 28, 5, false)
        )

        postAdapter = PostAdapter(posts) { post ->
            showAskQuestionDialog(post)
        }
        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = postAdapter

        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_connections -> {
                    startActivity(Intent(this, ConnectActivity::class.java))
                    true
                }
                R.id.nav_create_post -> {
                    startActivity(Intent(this, CreatePostActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    // Navigate to Messages Screen
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showAskQuestionDialog(post: Post) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_ask_question, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvQuestionTitle)
        val etQuestion = view.findViewById<TextInputEditText>(R.id.etQuestion)
        val rbPublic = view.findViewById<RadioButton>(R.id.rbPublic)
        val btnSend = view.findViewById<Button>(R.id.btnSendQuestion)

        tvTitle.text = "Ask ${post.userName} a question"

        btnSend.setOnClickListener {
            val question = etQuestion.text.toString()
            val isPublic = rbPublic.isChecked
            val type = if (isPublic) "Publicly" else "Privately"
            Toast.makeText(this, "Question sent $type!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
