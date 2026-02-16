package com.example.resumebuilder.di

import android.content.Context
import androidx.room.Room
import com.example.resumebuilder.data.local.AppDatabase
import com.example.resumebuilder.data.local.dao.UserProfileDao
import com.example.resumebuilder.data.remote.UserProfileRemoteDataSource
import com.example.resumebuilder.data.repository.UserProfileRepository
import com.example.resumebuilder.domain.usecase.GetUserProfileUseCase
import com.example.resumebuilder.domain.usecase.SaveUserProfileUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "resume_builder_db"
        ).build()
    }

    @Provides
    fun provideUserProfileDao(
        database: AppDatabase
    ): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(
        firestore: FirebaseFirestore
    ): UserProfileRemoteDataSource {
        return UserProfileRemoteDataSource(firestore)
    }
}
