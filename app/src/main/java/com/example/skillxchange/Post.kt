package com.example.skillxchange

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userTitle: String = "",
    val content: String = "",
    val timestamp: String = "",
    val likes: Int = 0,
    val comments: Int = 0,
    val hasVideo: Boolean = false,
    val imageUri: String? = null // New field to store the image URI
)
