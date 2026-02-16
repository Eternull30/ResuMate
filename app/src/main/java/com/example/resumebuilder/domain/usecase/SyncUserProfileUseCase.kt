package com.example.resumebuilder.domain.usecase

import com.example.resumebuilder.data.repository.UserProfileRepository

class SyncUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(uid: String): Result<Unit> {
        return repository.syncUserProfile(uid)
    }
}
