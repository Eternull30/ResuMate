package com.example.resumebuilder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.resumebuilder.data.local.dao.UserProfileDao
import com.example.resumebuilder.data.local.entity.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
}