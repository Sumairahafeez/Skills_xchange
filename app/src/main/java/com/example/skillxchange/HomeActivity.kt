package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillxchange.model.Message
import com.example.skillxchange.model.Notification
import com.example.skillxchange.model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.util.UUID

class HomeActivity : AppCompatActivity() {

    private lateinit var rvFeed: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        rvFeed = findViewById(R.id.rvUsers)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val topRightAvatar = findViewById<ShapeableImageView>(R.id.ivTopRightAvatar)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val btnNotifications = findViewById<View>(R.id.btnNotifications)
        val notificationDot = findViewById<View>(R.id.viewNotificationDot)

        topRightAvatar?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnNotifications?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    val intent = Intent(this@HomeActivity, ExploreActivity::class.java)
                    intent.putExtra("search_query", query)
                    startActivity(intent)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        syncProfileAndListen(topRightAvatar)
        listenForNewNotifications(notificationDot)

        postAdapter = PostAdapter(
            postList = emptyList(),
            currentUserId = currentUserId,
            onLikeClicked = { post -> handleLike(post) },
            onCommentClicked = { post -> 
                val intent = Intent(this, CommentsActivity::class.java)
                intent.putExtra("postId", post.id)
                startActivity(intent)
            },
            onDeleteClicked = { post -> handleDelete(post) },
            onAskQuestion = { post -> showAskQuestionDialog(post) },
            onRepostClicked = { post -> handleRepost(post) },
            onUserClicked = { userId ->
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        )

        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = postAdapter

        listenForPosts()

        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_connections -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.nav_create_post -> {
                    startActivity(Intent(this, CreatePostActivity::class.java))
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
    }

    private fun syncProfileAndListen(avatarView: ShapeableImageView?) {
        val userRef = db.collection("users").document(currentUserId)
        userRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                currentUser = snapshot.toObject(User::class.java)
                avatarView?.let {
                    Glide.with(this).load(currentUser?.photoUrl).placeholder(R.drawable.ic_user_placeholder).into(it)
                }
            } else if (auth.currentUser != null) {
                val firebaseUser = auth.currentUser
                val newUser = User(
                    uid = currentUserId,
                    name = firebaseUser?.displayName ?: "Member",
                    email = firebaseUser?.email ?: "",
                    tagline = "SkillXchange Explorer"
                )
                userRef.set(newUser)
            }
        }
    }

    private fun listenForNewNotifications(dot: View?) {
        db.collection("notifications")
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                if (!snapshot.isEmpty) {
                    dot?.visibility = View.VISIBLE
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val notification = dc.document.toObject(Notification::class.java)
                            NotificationHelper.showSystemNotification(this, notification)
                        }
                    }
                } else {
                    dot?.visibility = View.GONE
                }
            }
    }

    private fun listenForPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val posts = snapshot.toObjects(Post::class.java)
                    postAdapter.updatePosts(posts)
                }
            }
    }

    private fun handleLike(post: Post) {
        val postRef = db.collection("posts").document(post.id)
        if (post.likedBy.contains(currentUserId)) {
            postRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
        } else {
            postRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
            if (post.userId != currentUserId) {
                NotificationHelper.createNotification(
                    toUserId = post.userId,
                    fromUserId = currentUserId,
                    fromUserName = currentUser?.name ?: "Someone",
                    fromUserProfileUrl = currentUser?.photoUrl ?: "",
                    message = "liked your post",
                    type = "LIKE",
                    relatedId = post.id
                )
            }
        }
    }

    private fun handleDelete(post: Post) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("posts").document(post.id).delete()
                    .addOnSuccessListener { Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleRepost(originalPost: Post) {
        val repostId = UUID.randomUUID().toString()
        val repost = Post(
            id = repostId,
            userId = currentUserId,
            userName = currentUser?.name ?: "Member",
            userTitle = currentUser?.tagline ?: "Explorer",
            userPhotoUrl = currentUser?.photoUrl ?: "",
            content = "Reposted from ${originalPost.userName}: \n\n${originalPost.content}",
            imageUrl = originalPost.imageUrl,
            timestamp = null
        )
        
        db.collection("posts").document(repostId).set(repost)
            .addOnSuccessListener {
                Toast.makeText(this, "Post reposted successfully!", Toast.LENGTH_SHORT).show()
                NotificationHelper.createNotification(
                    toUserId = originalPost.userId,
                    fromUserId = currentUserId,
                    fromUserName = currentUser?.name ?: "Someone",
                    fromUserProfileUrl = currentUser?.photoUrl ?: "",
                    message = "reposted your post",
                    type = "REPOST",
                    relatedId = repostId
                )
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to repost", Toast.LENGTH_SHORT).show() }
    }

    private fun showAskQuestionDialog(post: Post) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_ask_question, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tvQuestionTitle)?.text = "Ask ${post.userName} a question"
        val etQuestion = view.findViewById<TextInputEditText>(R.id.etQuestion)
        val rbPublic = view.findViewById<RadioButton>(R.id.rbPublic)

        view.findViewById<Button>(R.id.btnSendQuestion)?.setOnClickListener {
            val questionText = etQuestion?.text?.toString()?.trim() ?: ""
            if (questionText.isEmpty()) {
                Toast.makeText(this, "Please type a question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()

            if (rbPublic.isChecked) {
                val commentId = UUID.randomUUID().toString()
                val commentData = hashMapOf(
                    "commentId" to commentId,
                    "postId" to post.id,
                    "userId" to currentUserId,
                    "userName" to (currentUser?.name ?: "Unknown"),
                    "userPhotoUrl" to (currentUser?.photoUrl ?: ""),
                    "text" to questionText,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("posts").document(post.id).collection("comments").document(commentId).set(commentData)
                    .addOnSuccessListener {
                        db.collection("posts").document(post.id).update("commentsCount", FieldValue.increment(1))
                        Toast.makeText(this, "Question posted publicly", Toast.LENGTH_SHORT).show()
                        if (post.userId != currentUserId) {
                            NotificationHelper.createNotification(
                                toUserId = post.userId,
                                fromUserId = currentUserId,
                                fromUserName = currentUser?.name ?: "Someone",
                                fromUserProfileUrl = currentUser?.photoUrl ?: "",
                                message = "commented on your post",
                                type = "COMMENT",
                                relatedId = post.id
                            )
                        }
                    }
            } else {
                sendPrivateMessage(post, questionText)
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", post.userId)
                    putExtra("userName", post.userName)
                }
                startActivity(intent)
                Toast.makeText(this, "Chat opened for your question", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun sendPrivateMessage(post: Post, text: String) {
        val otherUserId = post.userId
        val chatId = if (currentUserId < otherUserId) "${currentUserId}_${otherUserId}" else "${otherUserId}_${currentUserId}"
        val message = Message(id = UUID.randomUUID().toString(), senderId = currentUserId, text = text)

        db.runBatch { batch ->
            batch.set(db.collection("chats").document(chatId).collection("messages").document(message.id), message)
            val chatData = mapOf(
                "id" to chatId,
                "participants" to listOf(currentUserId, otherUserId),
                "lastMessage" to text,
                "lastSenderId" to currentUserId,
                "lastTimestamp" to FieldValue.serverTimestamp(),
                "userNames" to mapOf(currentUserId to (currentUser?.name ?: ""), otherUserId to post.userName),
                "userPhotos" to mapOf(currentUserId to (currentUser?.photoUrl ?: ""), otherUserId to post.userPhotoUrl)
            )
            batch.set(db.collection("chats").document(chatId), chatData, SetOptions.merge())
        }.addOnSuccessListener {
            NotificationHelper.createNotification(
                toUserId = otherUserId,
                fromUserId = currentUserId,
                fromUserName = currentUser?.name ?: "Someone",
                fromUserProfileUrl = currentUser?.photoUrl ?: "",
                message = text,
                type = "MESSAGE",
                relatedId = chatId
            )
        }
    }
}
