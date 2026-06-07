package com.example.skillxchange.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val mediaUrl: String? = null,
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    @field:JvmField
    val isSeen: Boolean = false
)
