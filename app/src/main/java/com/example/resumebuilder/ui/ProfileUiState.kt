package com.example.resumebuilder.ui.state

import com.example.resumebuilder.domain.model.UserProfile

sealed class ProfileUiState {

    object Loading : ProfileUiState()

    data class Success(
        val profile: UserProfile
    ) : ProfileUiState()

    data class Error(
        val message: String
    ) : ProfileUiState()
}
