package com.example.resumebuilder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.resumebuilder.domain.model.Resume
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ResumeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _resumes = MutableStateFlow<List<Resume>>(emptyList())
    val resumes: StateFlow<List<Resume>> = _resumes

    // Track current user to detect login/logout changes
    private var currentUserId: String? = null

    // Track listener registration to prevent duplicates
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        loadResumes()
    }

    private fun loadResumes() {
        val uid = auth.currentUser?.uid

        Log.d("ResumeViewModel", "loadResumes called. Current UID: $uid, Previous UID: $currentUserId")

        // IMPORTANT: If user changed (logout then login), clear cache
        if (currentUserId != uid) {
            Log.d("ResumeViewModel", "User ID changed! Clearing cache.")
            Log.d("ResumeViewModel", "Old user: $currentUserId")
            Log.d("ResumeViewModel", "New user: $uid")

            // Clear old listener
            listenerRegistration?.remove()

            // Clear old resume data
            _resumes.value = emptyList()

            // Update current user
            currentUserId = uid
        }

        if (uid == null) {
            Log.e("ResumeViewModel", "ERROR: User not authenticated!")
            _resumes.value = emptyList()  // Clear on logout
            currentUserId = null
            return
        }

        Log.d("ResumeViewModel", "Setting up listener for user: $uid")

        // Remove old listener before setting new one
        listenerRegistration?.remove()

        // Set up new listener
        listenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("ResumeViewModel", "ERROR loading resumes: ${error.message}")
                    _resumes.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Resume::class.java)?.copy(id = it.id)
                } ?: emptyList()

                Log.d("ResumeViewModel", "Loaded ${list.size} resumes from Firestore for user: $uid")
                list.forEach {
                    Log.d("ResumeViewModel", "  - ${it.id}: ${it.title}")
                }

                _resumes.value = list.sortedByDescending { it.createdAt }
            }
    }

    fun createResume(title: String, templateType: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e("ResumeViewModel", "ERROR: User not authenticated!")
            return
        }

        Log.d("ResumeViewModel", "Creating resume: $title for user: $uid")

        val id = UUID.randomUUID().toString()

        val resume = Resume(
            id = id,
            title = title,
            templateType = templateType,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(id)
            .set(resume)
            .addOnSuccessListener {
                Log.d("ResumeViewModel", "SUCCESS: Resume created: $id")
            }
            .addOnFailureListener { exception ->
                Log.e("ResumeViewModel", "ERROR creating resume: ${exception.message}")
            }
    }

    fun deleteResume(resumeId: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e("ResumeViewModel", "ERROR: User not authenticated!")
            return
        }

        Log.d("ResumeViewModel", "Deleting resume: $resumeId for user: $uid")

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(resumeId)
            .delete()
            .addOnSuccessListener {
                Log.d("ResumeViewModel", "SUCCESS: Resume deleted: $resumeId")
            }
            .addOnFailureListener { exception ->
                Log.e("ResumeViewModel", "ERROR deleting resume: ${exception.message}")
            }
    }

    fun renameResume(resumeId: String, newTitle: String) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e("ResumeViewModel", "ERROR: User not authenticated!")
            return
        }

        Log.d("ResumeViewModel", "Renaming resume: $resumeId to $newTitle for user: $uid")

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(resumeId)
            .update("title", newTitle)
            .addOnSuccessListener {
                Log.d("ResumeViewModel", "SUCCESS: Resume renamed: $resumeId")
            }
            .addOnFailureListener { exception ->
                Log.e("ResumeViewModel", "ERROR renaming resume: ${exception.message}")
            }
    }

    fun updateResume(resume: Resume) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Log.e("ResumeViewModel", "ERROR: User not authenticated!")
            return
        }

        Log.d("ResumeViewModel", "Updating resume: ${resume.id} for user: $uid")

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(resume.id)
            .set(resume)
            .addOnSuccessListener {
                Log.d("ResumeViewModel", "SUCCESS: Resume updated: ${resume.id}")
            }
            .addOnFailureListener { exception ->
                Log.e("ResumeViewModel", "ERROR updating resume: ${exception.message}")
            }
    }

    // IMPORTANT: Call this on logout
    fun clearCache() {
        Log.d("ResumeViewModel", "Clearing cache on logout")
        listenerRegistration?.remove()
        _resumes.value = emptyList()
        currentUserId = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ResumeViewModel", "ViewModel cleared, removing listener")
        listenerRegistration?.remove()
    }
}