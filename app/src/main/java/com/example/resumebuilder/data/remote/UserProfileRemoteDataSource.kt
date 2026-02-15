package com.example.resumebuilder.data.remote

import com.example.resumebuilder.data.local.entity.UserProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserProfileRemoteDataSource(
    private val firestore: FirebaseFirestore
) {

    private val collection = firestore.collection("user_profiles")

    suspend fun getUserProfile(uid: String): UserProfileEntity? {
        val snapshot = collection.document(uid).get().await()
        return snapshot.toObject(UserProfileEntity::class.java)
    }

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        collection.document(profile.uid).set(profile).await()
    }
}
