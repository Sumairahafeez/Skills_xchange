package com.example.skillxchange

import com.google.firebase.Timestamp

// Unifying with com.example.skillxchange.model.User to avoid confusion
data class User(
    val uid: String = "",
    val id: String = "", // for backward compatibility if needed
    val name: String = "",
    val email: String = "",
    val tagline: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val skills: List<String> = emptyList(),
    val connections: List<String> = emptyList(),
    val teachSkills: List<String> = emptyList(),
    val learnSkills: List<String> = emptyList(),
    val rating: Double = 4.5,
    val createdAt: Timestamp? = null
)
