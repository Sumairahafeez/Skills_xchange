package com.example.skillsexchange

data class User(
    val id: String = "",
    val name: String = "",
    val tagline: String = "",
    val bio: String = "",
    val teachSkills: List<String> = emptyList(),
    val learnSkills: List<String> = emptyList(),
    val rating: Double = 4.5
)