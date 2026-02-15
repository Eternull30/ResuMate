package com.example.resumebuilder.domain.usecase

import com.example.resumebuilder.data.repository.UserProfileRepository

class SyncUserProfile(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(uid: String) {
        repository.syncUserProfile(uid)
    }
}