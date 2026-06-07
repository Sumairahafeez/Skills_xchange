package com.example.skillxchange.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    @ServerTimestamp
    val lastTimestamp: Timestamp? = null,
    val userNames: Map<String, String> = emptyMap(),
    val userPhotos: Map<String, String> = emptyMap()
)
