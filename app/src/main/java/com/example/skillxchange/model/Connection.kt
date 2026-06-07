package com.example.skillxchange.model

import com.google.firebase.Timestamp

data class Connection(
    val id: String = "", // Format: "uid1_uid2" (alphabetically sorted)
    val users: List<String> = emptyList(),
    val status: String = "pending", // "pending", "accepted", "blocked"
    val requestSenderId: String = "",
    val updatedAt: Timestamp = Timestamp.now()
)
