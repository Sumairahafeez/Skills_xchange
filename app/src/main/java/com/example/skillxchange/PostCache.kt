package com.example.skillxchange

import android.content.Context
import com.google.firebase.database.*

object PostCache {
    private val database = FirebaseDatabase.getInstance().getReference("posts")
    private val commentsDatabase = FirebaseDatabase.getInstance().getReference("post_comments")
    private var cachedPosts = mutableListOf<Post>()

    init {
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Post>()
                for (child in snapshot.children) {
                    child.getValue(Post::class.java)?.let { list.add(0, it) }
                }
                cachedPosts = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun savePost(context: Context, post: Post) {
        val key = post.id.ifEmpty { database.push().key ?: return }
        val finalPost = if (post.id.isEmpty()) post.copy(id = key) else post
        database.child(key).setValue(finalPost)
    }

    fun getAllPosts(context: Context): List<Post> = cachedPosts

    fun listenToPosts(callback: (List<Post>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Post>()
                for (child in snapshot.children) {
                    child.getValue(Post::class.java)?.let { list.add(0, it) }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun toggleLike(postId: String, currentLikes: Int, isAdding: Boolean) {
        val newLikes = if (isAdding) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
        database.child(postId).child("likes").setValue(newLikes)
    }

    fun addComment(postId: String, comment: String, currentCommentCount: Int) {
        val commentId = commentsDatabase.child(postId).push().key ?: return
        commentsDatabase.child(postId).child(commentId).setValue(comment)
        database.child(postId).child("comments").setValue(currentCommentCount + 1)
    }

    fun listenToComments(postId: String, callback: (List<String>) -> Unit) {
        commentsDatabase.child(postId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { list.add(it) }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
