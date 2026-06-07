package com.example.skillxchange

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.Comment
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class CommentsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var postId: String
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        postId = intent.getStringExtra("postId") ?: ""
        if (postId.isEmpty()) {
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSendComment = findViewById<MaterialButton>(R.id.btnSendComment)

        commentAdapter = CommentAdapter(emptyList())
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        fetchCurrentUser()
        listenForComments()

        btnSendComment.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                postComment(text)
                etComment.text.clear()
            }
        }
    }

    private fun fetchCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentUser = doc.toObject(User::class.java)
            }
    }

    private fun listenForComments() {
        db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val comments = snapshot.toObjects(Comment::class.java)
                    commentAdapter.updateComments(comments)
                }
            }
    }

    private fun postComment(text: String) {
        val uid = auth.currentUser?.uid ?: return
        val commentId = UUID.randomUUID().toString()
        val comment = Comment(
            commentId = commentId,
            userId = uid,
            userName = currentUser?.name ?: "Unknown",
            userPhotoUrl = currentUser?.photoUrl ?: "",
            text = text
        )

        val postRef = db.collection("posts").document(postId)
        
        // Use a batch or run as separate tasks
        postRef.collection("comments").document(commentId).set(comment)
            .addOnSuccessListener {
                postRef.update("commentsCount", FieldValue.increment(1))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
            }
    }
}
