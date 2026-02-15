package com.example.resumebuilder.data.mapper

import com.example.resumebuilder.data.local.entity.UserProfileEntity
import com.example.resumebuilder.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        uid = uid,
        name = name,
        email = email,
        bio = bio,
        skills = skills,
        experience = experience,
        lastUpdated = lastUpdated
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        uid = uid,
        name = name,
        email = email,
        bio = bio,
        skills = skills,
        experience = experience,
        lastUpdated = lastUpdated
    )
}
