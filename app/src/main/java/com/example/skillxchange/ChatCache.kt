package com.example.skillxchange

import android.content.Context
import com.google.firebase.database.*

object ChatCache {
    private val database = FirebaseDatabase.getInstance().getReference("chats")
    private val viewedDatabase = FirebaseDatabase.getInstance().getReference("viewed_status")
    
    private val lastMessages = mutableMapOf<String, ChatMessage>()
    private val viewedStatus = mutableMapOf<String, Boolean>()

    private fun chatKey(currentUserId: String, otherUserId: String): String {
        val sortedIds = listOf(currentUserId, otherUserId).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }

    init {
        // Global listener to keep track of last messages for the list view
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatSnapshot in snapshot.children) {
                    val lastMsg = chatSnapshot.children.lastOrNull()?.getValue(ChatMessage::class.java)
                    if (lastMsg != null) {
                        lastMessages[chatSnapshot.key!!] = lastMsg
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun save(context: Context, currentUserId: String, otherUserId: String, message: ChatMessage) {
        val key = chatKey(currentUserId, otherUserId)
        database.child(key).push().setValue(message)
        markViewed(otherUserId, currentUserId, false)
    }

    fun listenToMessages(currentUserId: String, otherUserId: String, callback: (List<ChatMessage>) -> Unit) {
        val key = chatKey(currentUserId, otherUserId)
        database.child(key).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    child.getValue(ChatMessage::class.java)?.let { list.add(it) }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun markViewed(myId: String, otherId: String, viewed: Boolean) {
        viewedDatabase.child(myId).child(otherId).setValue(viewed)
    }

    // Compatibility methods for synchronous calls in Adapters
    fun load(context: Context, currentUserId: String, otherUserId: String): List<ChatMessage> {
        val key = chatKey(currentUserId, otherUserId)
        val last = lastMessages[key]
        return if (last != null) listOf(last) else emptyList()
    }

    fun isViewed(context: Context, myId: String, otherId: String): Boolean {
        return viewedStatus["${myId}_${otherId}"] ?: true
    }

    fun markViewed(context: Context, myId: String, otherId: String, viewed: Boolean) {
        markViewed(myId, otherId, viewed)
    }

    fun listenToAllUnviewed(myId: String, callback: (Boolean) -> Unit) {
        viewedDatabase.child(myId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var hasUnviewed = false
                for (child in snapshot.children) {
                    val isViewed = child.getValue(Boolean::class.java) ?: true
                    viewedStatus["${myId}_${child.key}"] = isViewed
                    if (!isViewed) hasUnviewed = true
                }
                callback(hasUnviewed)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun listenToViewedStatus(myId: String, otherId: String, callback: (Boolean) -> Unit) {
        viewedDatabase.child(myId).child(otherId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(Boolean::class.java) ?: true
                viewedStatus["${myId}_${otherId}"] = status
                callback(status)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    
    fun hasAnyUnviewed(context: Context, myId: String): Boolean = false
}
