package com.example.resumebuilder.viewmodel

import androidx.lifecycle.ViewModel
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage

    fun sendPasswordResetEmail(email: String, callback: (Boolean) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Password reset email sent successfully")
                    callback(true)
                } else {
                    Log.e("AuthViewModel", "Failed to send reset email: ${task.exception?.message}")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Exception: ${exception.message}")
                callback(false)
            }
    }

    fun syncUserData(userId: String, callback: (Boolean) -> Unit) {
        if (userId.isEmpty()) {
            callback(false)
            return
        }

        _isSyncing.value = true
        _syncMessage.value = "Syncing data..."

        firestore.collection("users")
            .document(userId)
            .collection("resumes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                _isSyncing.value = false
                _syncMessage.value = "Sync complete"
                Log.d("AuthViewModel", "Sync successful: ${querySnapshot.size()} resumes synced")
                callback(true)
            }
            .addOnFailureListener { exception ->
                _isSyncing.value = false
                _syncMessage.value = "Sync failed: ${exception.message}"
                Log.e("AuthViewModel", "Sync failed: ${exception.message}")
                callback(false)
            }
    }

    fun clearSyncMessage() {
        _syncMessage.value = ""
    }

    fun resetSyncState() {
        _isSyncing.value = false
        _syncMessage.value = ""
    }
}