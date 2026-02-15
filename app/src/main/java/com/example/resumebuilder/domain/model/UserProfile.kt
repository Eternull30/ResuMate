package com.example.resumebuilder.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val bio: String,
    val skills: String,
    val experience: String,
    val lastUpdated: Long
)
