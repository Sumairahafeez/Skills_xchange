package com.example.skillxchange.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val tagline: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val skills: List<String> = emptyList(),
    val connections: List<String> = emptyList(),
    val teachSkills: List<String> = emptyList(),
    val learnSkills: List<String> = emptyList(),
    val rating: Double = 4.8,
    val profileViews: Long = 0,
    val viewedBy: List<String> = emptyList(), // Store UIDs of people who viewed this profile
    val createdAt: Timestamp? = null
)
