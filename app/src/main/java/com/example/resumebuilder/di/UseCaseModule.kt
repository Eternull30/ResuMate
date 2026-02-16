package com.example.resumebuilder.di

import com.example.resumebuilder.data.repository.UserProfileRepository
import com.example.resumebuilder.domain.usecase.GetUserProfileUseCase
import com.example.resumebuilder.domain.usecase.SaveUserProfileUseCase
import com.example.resumebuilder.domain.usecase.SyncUserProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(
        repository: UserProfileRepository
    ): GetUserProfileUseCase {
        return GetUserProfileUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveUserProfileUseCase(
        repository: UserProfileRepository
    ): SaveUserProfileUseCase {
        return SaveUserProfileUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSyncUserProfileUseCase(
        repository: UserProfileRepository
    ): SyncUserProfileUseCase {
        return SyncUserProfileUseCase(repository)
    }

}

