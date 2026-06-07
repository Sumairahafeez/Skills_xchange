package com.example.skillxchange

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userTitle: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val likedBy: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val tags: List<String> = emptyList()
) {
    val likesCount: Int get() = likedBy.size
}
