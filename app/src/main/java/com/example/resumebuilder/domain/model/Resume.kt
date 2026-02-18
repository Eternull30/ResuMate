package com.example.resumebuilder.domain.model

data class Resume(
    val id: String = "",
    val title: String = "",
    val templateType: String = "",
    val createdAt: Long = 0L
)
