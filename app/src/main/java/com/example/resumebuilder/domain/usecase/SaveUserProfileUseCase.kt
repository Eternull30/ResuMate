package com.example.resumebuilder.domain.usecase

import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.data.repository.UserProfileRepository

class SaveUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        return repository.saveUserProfile(profile)
    }
}

