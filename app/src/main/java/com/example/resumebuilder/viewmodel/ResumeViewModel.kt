package com.example.resumebuilder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.resumebuilder.domain.model.Resume
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _resumes = MutableStateFlow<List<Resume>>(emptyList())
    val resumes: StateFlow<List<Resume>> = _resumes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var currentUserId: String? = null
    private var listenerRegistration: ListenerRegistration? = null

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            Log.d("ResumeViewModel", "Auth state changed. UID = $uid")
            loadResumesForUser(uid)
        }
    }
    private fun loadResumesForUser(uid: String?) {

        Log.d("ResumeViewModel", "loadResumesForUser: $uid")

        // Remove old listener ALWAYS
        listenerRegistration?.remove()
        listenerRegistration = null

        _resumes.value = emptyList()

        if (uid == null) {
            _isLoading.value = false
            currentUserId = null
            return
        }

        currentUserId = uid
        _isLoading.value = true

        listenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("ResumeViewModel", error.message ?: "Error")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Resume::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _resumes.value = list.sortedByDescending { it.createdAt }
                _isLoading.value = false
            }
    }

//    private fun loadResumes() {
//        val uid = auth.currentUser?.uid
//
//        Log.d("ResumeViewModel", "loadResumes called. Current UID: $uid, Previous UID: $currentUserId")
//
//        // If user changed (logout then login), clear cache
//        if (currentUserId != uid) {
//            Log.d("ResumeViewModel", "User ID changed! Clearing cache.")
//            Log.d("ResumeViewModel", "Old user: $currentUserId")
//            Log.d("ResumeViewModel", "New user: $uid")
//
//            listenerRegistration?.remove()
//            _resumes.value = emptyList()
//            _isLoading.value = true  // START LOADING
//            currentUserId = uid
//        }
//
//        if (uid == null) {
//            Log.e("ResumeViewModel", "ERROR: User not authenticated!")
//            _resumes.value = emptyList()
//            _isLoading.value = false
//            currentUserId = null
//            return
//        }
//
//        Log.d("ResumeViewModel", "Setting up listener for user: $uid")
//        _isLoading.value = true  // IMPORTANT: Set to true BEFORE listener
//
//        // Remove old listener before setting new one
//        listenerRegistration?.remove()
//
//        // Set up listener
//        listenerRegistration = firestore.collection("users")
//            .document(uid)
//            .collection("resumes")
//            .addSnapshotListener { snapshot, error ->
//
//                Log.d("ResumeViewModel", "Listener fired!")
//
//                if (error != null) {
//                    Log.e("ResumeViewModel", "ERROR in listener: ${error.message}")
//                    _resumes.value = emptyList()
//                    _isLoading.value = false
//                    return@addSnapshotListener
//                }
//
//                if (snapshot == null) {
//                    Log.d("ResumeViewModel", "Snapshot is null")
//                    _resumes.value = emptyList()
//                    _isLoading.value = false
//                    return@addSnapshotListener
//                }
//
//                Log.d("ResumeViewModel", "Snapshot documents: ${snapshot.documents.size}")
//
//                val list = snapshot.documents.mapNotNull { doc ->
//                    try {
//                        val resume = doc.toObject(Resume::class.java)?.copy(id = doc.id)
//                        Log.d("ResumeViewModel", "Parsed resume: ${resume?.id} - ${resume?.title}")
//                        resume
//                    } catch (e: Exception) {
//                        Log.e("ResumeViewModel", "Error parsing resume: ${e.message}")
//                        null
//                    }
//                }
//
//                Log.d("ResumeViewModel", "Final list size: ${list.size}")
//
//                _resumes.value = list.sortedByDescending { it.createdAt }
//                _isLoading.value = false  // IMPORTANT: Stop loading
//
//                Log.d("ResumeViewModel", "Updated UI with ${list.size} resumes")
//            }
//    }

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

    fun clearCache() {
        Log.d("ResumeViewModel", "Clearing cache on logout")
        listenerRegistration?.remove()
        _resumes.value = emptyList()
        _isLoading.value = false
        currentUserId = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ResumeViewModel", "ViewModel cleared, removing listener")
        listenerRegistration?.remove()
    }
}