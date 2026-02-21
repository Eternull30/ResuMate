package com.example.resumebuilder.domain.model

data class Resume(
    val id: String = "",
    val title: String = "",
    val templateType: String = "modern",
    val createdAt: Long = 0L,

    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val summary: String = "",

    val skills: List<String> = emptyList(),
    val experience: List<String> = emptyList(),
    val projects: List<String> = emptyList(),
    val education: List<String> = emptyList(),
)


