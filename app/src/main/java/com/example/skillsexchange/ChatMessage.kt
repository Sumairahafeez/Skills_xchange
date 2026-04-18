package com.example.skillsexchange

data class ChatMessage(
    val text: String,
    val isSentByMe: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val sharedPostId: String? = null // If not null, this message represents a shared post
)