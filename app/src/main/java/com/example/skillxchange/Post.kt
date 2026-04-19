package com.example.skillxchange

data class Post(
    val id: String = "",
    val userId: String = "", // Added userId to track who created the post
    val userName: String = "",
    val userTitle: String = "",
    val content: String = "",
    val timestamp: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val hasVideo: Boolean = false
)
