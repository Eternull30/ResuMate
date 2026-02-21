package com.example.resumebuilder.data.remote.gemini

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {

    @POST("v1/models/gemini-1.5-pro-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
