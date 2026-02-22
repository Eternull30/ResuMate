package com.example.resumebuilder.data.repository

import com.example.resumebuilder.data.local.dao.UserProfileDao
import com.example.resumebuilder.data.mapper.toDomain
import com.example.resumebuilder.data.mapper.toEntity
import com.example.resumebuilder.data.remote.UserProfileRemoteDataSource
import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
    private val remote: UserProfileRemoteDataSource
) : UserProfileRepository {

    override fun getUserProfile(uid: String): Flow<UserProfile?> {
        return dao.getUserProfile(uid).map { it?.toDomain() }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val entity = profile.toEntity()

            dao.insertUserProfile(entity)

            remote.saveUserProfile(entity)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun syncUserProfile(uid: String): Result<Unit> {
        return try {

            val local = dao.getUserProfileOnce(uid)
            val remoteProfile = remote.getUserProfile(uid)

            when {
                local == null && remoteProfile != null -> {
                    dao.insertUserProfile(remoteProfile)
                }

                local != null && remoteProfile == null -> {
                    remote.saveUserProfile(local)
                }

                local != null && remoteProfile != null -> {
                    if (remoteProfile.lastUpdated > local.lastUpdated) {
                        dao.insertUserProfile(remoteProfile)
                    } else {
                        remote.saveUserProfile(local)
                    }
                }
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

