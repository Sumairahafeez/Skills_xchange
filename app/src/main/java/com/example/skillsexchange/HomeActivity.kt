package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter

    private val seedPosts = mutableListOf(
        Post("1", "Alex Thompson", "Kotlin Expert", "Just finished a tutorial on Coroutines! Check it out.", "2h ago", 15, 3, false),
        Post("2", "Sarah Jenkins", "UI Designer", "New Figma shortcuts that will save you hours.", "5h ago", 42, 10, true),
        Post("3", "Michael Brown", "Public Speaker", "How to overcome stage fright in 5 steps.", "1d ago", 28, 5, false)
    )

    private val createPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                refreshFeed()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Read logged-in user's name from SharedPreferences
        val prefs    = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Osama") ?: "Osama"

        findViewById<TextView>(R.id.tvWelcome).text = "Skill Feed"
        findViewById<View>(R.id.filterLayout).visibility = View.GONE
        findViewById<TextView>(R.id.tvProfileName).text = userName

        rvFeed = findViewById(R.id.rvUsers)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        postAdapter = PostAdapter(buildFeedList()) { post -> showAskQuestionDialog(post) }
        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = postAdapter

        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_connections -> {
                    startActivity(Intent(this, ConnectActivity::class.java)); true
                }
                R.id.nav_create_post -> {
                    createPostLauncher.launch(Intent(this, CreatePostActivity::class.java)); true
                }
                R.id.nav_messages -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)); true
                }
                else -> false
            }
        }
    }

    private fun buildFeedList(): MutableList<Post> {
        val combined = mutableListOf<Post>()
        combined.addAll(CreatePostActivity.sharedPosts)
        combined.addAll(seedPosts)
        return combined
    }

    private fun refreshFeed() {
        postAdapter.updatePosts(buildFeedList())
        rvFeed.scrollToPosition(0)
        Toast.makeText(this, "Your post is live on the feed!", Toast.LENGTH_SHORT).show()
    }

    private fun showAskQuestionDialog(post: Post) {
        val dialog = BottomSheetDialog(this)
        val view   = LayoutInflater.from(this).inflate(R.layout.dialog_ask_question, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvQuestionTitle).text = "Ask ${post.userName} a question"
        val rbPublic = view.findViewById<RadioButton>(R.id.rbPublic)

        view.findViewById<Button>(R.id.btnSendQuestion).setOnClickListener {
            val type = if (rbPublic.isChecked) "Publicly" else "Privately"
            Toast.makeText(this, "Question sent $type!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }
}