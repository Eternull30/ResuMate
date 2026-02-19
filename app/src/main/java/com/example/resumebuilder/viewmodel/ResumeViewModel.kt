package com.example.resumebuilder.viewmodel

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

    init {
        loadResumes()
    }

    private fun loadResumes() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .addSnapshotListener { snapshot, _ ->

                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Resume::class.java)?.copy(id = it.id)
                } ?: emptyList()

                _resumes.value = list.sortedByDescending { it.createdAt }
            }
    }

    fun createResume(title: String, templateType: String) {
        val uid = auth.currentUser?.uid ?: return
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
    }

    fun deleteResume(resumeId: String) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(resumeId)
            .delete()
    }

    fun renameResume(resumeId: String, newTitle: String) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(resumeId)
            .update("title", newTitle)
    }
    fun updateResume(resume: Resume) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .collection("resumes")
            .document(resume.id)
            .set(resume)
    }


}
