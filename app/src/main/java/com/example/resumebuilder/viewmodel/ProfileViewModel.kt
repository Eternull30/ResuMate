package com.example.resumebuilder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.domain.usecase.GetUserProfileUseCase
import com.example.resumebuilder.domain.usecase.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfile: GetUserProfileUseCase,
    private val saveUserProfile: SaveUserProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UserProfile?>(null)
    val state: StateFlow<UserProfile?> = _state

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadUser(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getUserProfile(uid).collect { profile ->
                _state.value = profile
                _isLoading.value = false
            }
        }
    }

    fun save(profile: UserProfile) {
        viewModelScope.launch {
            saveUserProfile(profile)
        }
    }
}
