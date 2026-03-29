package com.example.skillsexchange

data class User(
    val name: String,
    val skillOffered: String,
    val bio: String,
    val rating: Double = 4.5
)