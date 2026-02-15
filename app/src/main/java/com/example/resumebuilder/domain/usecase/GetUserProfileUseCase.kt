package com.example.resumebuilder.domain.usecase

import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class GetUserProfileUseCase(
    private val repository: UserProfileRepository
) {
    operator fun invoke(uid: String): Flow<UserProfile?> {
        return repository.getUserProfile(uid)
    }
}
