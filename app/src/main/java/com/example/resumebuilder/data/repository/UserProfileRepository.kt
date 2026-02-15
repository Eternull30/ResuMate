package com.example.resumebuilder.data.repository

import com.example.resumebuilder.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    fun getUserProfile(uid: String): Flow<UserProfile?>

    suspend fun saveUserProfile(profile: UserProfile)

    suspend fun syncUserProfile(uid: String)
}
