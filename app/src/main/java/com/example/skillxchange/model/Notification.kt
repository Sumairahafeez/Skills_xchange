package com.example.skillxchange.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Notification(
    val id: String = "",
    val toUserId: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserProfileUrl: String = "",
    val message: String = "",
    val type: String = "", // "LIKE", "COMMENT", "CONNECTION", "MESSAGE"
    val relatedId: String = "", // e.g., postId or chatId
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    @field:JvmField
    val isRead: Boolean = false
)
