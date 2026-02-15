package com.example.resumebuilder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(

    @PrimaryKey(autoGenerate = false)
    val uid: String,

    val name: String,

    val email: String,

    val bio: String,

    val skills: String,

    val experience: String,

    val lastUpdated: Long = System.currentTimeMillis()
)
