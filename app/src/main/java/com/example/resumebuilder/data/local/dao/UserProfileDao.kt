package com.example.resumebuilder.data.local.dao

import androidx.room.*
import com.example.resumebuilder.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    fun getUserProfile(uid: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE uid = :uid LIMIT 1")
    suspend fun getUserProfileOnce(uid: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)
}


