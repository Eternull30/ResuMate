package com.example.resumebuilder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.domain.usecase.GetUserProfileUseCase
import com.example.resumebuilder.domain.usecase.SaveUserProfileUseCase
import com.example.resumebuilder.domain.usecase.SyncUserProfileUseCase
import com.example.resumebuilder.ui.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileEvent {
    object NavigateToResume : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    private val syncUserProfile: SyncUserProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _event = MutableSharedFlow<ProfileEvent>()
    val event = _event.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving


    fun loadUser(uid: String) {
        viewModelScope.launch {

            _uiState.value = ProfileUiState.Loading

            syncUserProfile(uid)

            getUserProfile(uid).collect { profile ->

                if (profile != null) {
                    _uiState.value = ProfileUiState.Success(profile)
                } else {

                    val newProfile = UserProfile(
                        uid = uid,
                        name = "",
                        email = "",
                        bio = "",
                        skills = "",
                        experience = "",
                        lastUpdated = System.currentTimeMillis()
                    )

                    saveUserProfile(newProfile)
                    _uiState.value = ProfileUiState.Success(newProfile)
                }
            }
        }
    }

    fun save(profile: UserProfile) {
        viewModelScope.launch {

            _isSaving.value = true

            val result = saveUserProfile(profile)

            _isSaving.value = false

            result.onSuccess {
                _event.emit(ProfileEvent.NavigateToResume)
            }

            result.onFailure {
                _uiState.value = ProfileUiState.Error("Failed to save profile")
            }
        }
    }

}
