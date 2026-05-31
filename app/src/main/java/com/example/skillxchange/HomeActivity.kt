package com.example.skillxchange

import android.content.Context
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var currentUserId: String
    private lateinit var bottomNavigation: BottomNavigationView

    private var allPosts = mutableListOf<Post>()
    private var myFriends = setOf<String>()

    // Optional: Keep seed posts as initial content if database is empty
    private val seedPosts = listOf(
        Post("p1", "u1", "Alex Thompson", "Kotlin Expert", "Just finished a tutorial on Coroutines! Check it out.", "2h ago", 15, 3, false),
        Post("p2", "u2", "Sarah Jenkins", "UI Designer", "New Figma shortcuts that will save you hours.", "5h ago", 42, 10, false),
        Post("p3", "u3", "Michael Brown", "Public Speaker", "How to overcome stage fright in 5 steps.", "1d ago", 28, 5, false)
    )

    private val createPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Firebase listeners will handle the update
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        val prefs = getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("user_id", "") ?: ""
        
        rvFeed = findViewById(R.id.rvUsers)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        val topRightAvatar = findViewById<ShapeableImageView>(R.id.ivTopRightAvatar)

        topRightAvatar?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Initialize adapter with empty list, will be populated by Firebase listeners
        postAdapter = PostAdapter(mutableListOf()) { post ->
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

        setupFirebaseListeners()
    }

    private fun setupFirebaseListeners() {
        // Listen for connection changes to update the feed and badges
        ConnectionCache.listenToFriends(currentUserId) { friends ->
            myFriends = friends
            refreshFeed()
        }

        // Listen for new posts from Firebase
        PostCache.listenToPosts { posts ->
            allPosts = posts.toMutableList()
            refreshFeed()
        }

        // Listen for pending invitations for the connection badge
        ConnectionCache.listenToInvitations(currentUserId) { invs ->
            if (invs.isNotEmpty()) {
                bottomNavigation.getOrCreateBadge(R.id.nav_connections).number = invs.size
            } else {
                bottomNavigation.removeBadge(R.id.nav_connections)
            }
        }

        // Listen for unread messages for the chat badge
        ChatCache.listenToAllUnviewed(currentUserId) { hasUnviewed ->
            bottomNavigation.getOrCreateBadge(R.id.nav_messages).isVisible = hasUnviewed
        }
    }

    private fun buildDisplayList(): MutableList<Post> {
        val displayList = mutableListOf<Post>()
        
        // Add Firebase posts first
        displayList.addAll(allPosts)
        
        // Add seed posts if Firebase is empty (optional)
        if (allPosts.isEmpty()) {
            displayList.addAll(seedPosts)
        }
        
        // Filter: Show only posts from friends OR posts created by the current user OR seed posts
        return displayList.filter { 
            it.userId == currentUserId || myFriends.contains(it.userId) || it.id.startsWith("p") 
        }.toMutableList()
    }

    private fun refreshFeed() {
        postAdapter.updatePosts(buildDisplayList())
    }

    private fun filterPosts(query: String?) {
        val baseList = buildDisplayList()
        if (query.isNullOrBlank()) {
            postAdapter.updatePosts(baseList)
            return
        }
        val filtered = baseList.filter {
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
        val rbPublic = view.findViewById<RadioButton>(R.id.rbPublic)

        view.findViewById<Button>(R.id.btnSendQuestion)?.setOnClickListener {
            val questionText = etQuestion?.text?.toString()?.trim()

            if (questionText.isNullOrEmpty()) {
                Toast.makeText(this, "Please type a question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()

            if (rbPublic.isChecked) {
                // Synced with Firebase via PostCache
                PostCache.addComment(post.id, questionText, post.comments)
                Toast.makeText(this, "Question posted publicly!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", post.userId)
                    putExtra("userName", post.userName)
                    putExtra("userTagline", post.userTitle)
                    putExtra("prefilledQuestion", questionText)
                }
                startActivity(intent)
            }
        }
        dialog.show()
    }
}
