package com.example.skillxchange

data class ChatMessage(
    val text: String,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sharedPostId: String? = null // If not null, this message represents a shared post
)