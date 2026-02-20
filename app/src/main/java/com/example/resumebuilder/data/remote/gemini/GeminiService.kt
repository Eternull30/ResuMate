package com.example.resumebuilder.data.remote.gemini

import com.example.resumebuilder.BuildConfig
import com.example.resumebuilder.domain.model.Resume
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    suspend fun improveResume(resume: Resume): String {

        val prompt = """
            Improve this resume summary professionally:
            ${resume.summary}
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(prompt)
                    )
                )
            )
        )

        return try {
            val response = api.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )

            response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: "No response"

                ?: "No response"

        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }
    }
}

