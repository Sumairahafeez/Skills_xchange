package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter

    private val seedPosts = mutableListOf(
        Post("1", "Alex Thompson", "Kotlin Expert", "Just finished a tutorial on Coroutines! Check it out.", "2h ago", 15, 3, false),
        Post("2", "Sarah Jenkins", "UI Designer", "New Figma shortcuts that will save you hours.", "5h ago", 42, 10, false),
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

        // Initialize views
        rvFeed = findViewById(R.id.rvUsers)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

        // === FIXED: Top-right avatar click → Open Profile ===
        // First give it an ID in the layout, then use it here
        val topRightAvatar = findViewById<ShapeableImageView>(R.id.ivTopRightAvatar)

        topRightAvatar?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Setup RecyclerView
        postAdapter = PostAdapter(buildFeedList()) { post ->
            showAskQuestionDialog(post)
        }

        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = postAdapter

        // Bottom Navigation
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_connections -> {
                    startActivity(Intent(this, ConnectActivity::class.java))
                    true
                }
                R.id.nav_create_post -> {
                    createPostLauncher.launch(Intent(this, CreatePostActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Search functionality
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterPosts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPosts(newText)
                return true
            }
        })
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

    private fun filterPosts(query: String?) {
        if (query.isNullOrBlank()) {
            postAdapter.updatePosts(buildFeedList())
            return
        }

        val filtered = buildFeedList().filter {
            it.userName.contains(query, ignoreCase = true) ||
                    it.userTitle.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
        }.toMutableList()

        postAdapter.updatePosts(filtered)
    }

    private fun showAskQuestionDialog(post: Post) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_ask_question, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvQuestionTitle)?.text = "Ask ${post.userName} a question"

        val etQuestion = view.findViewById<TextInputEditText>(R.id.etQuestion)
        view.findViewById<Button>(R.id.btnSendQuestion)?.setOnClickListener {
            val questionText = etQuestion?.text?.toString()?.trim()

            if (questionText.isNullOrEmpty()) {
                Toast.makeText(this, "Please type a question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()

            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("userId", post.id)
                putExtra("userName", post.userName)
                putExtra("userTagline", post.userTitle)
                putExtra("prefilledQuestion", questionText)
            }
            startActivity(intent)
        }
        dialog.show()
    }
}