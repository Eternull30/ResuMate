package com.example.resumebuilder.data.remote.gemini

import android.util.Log
import com.example.resumebuilder.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

// Data classes
data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)
data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: ContentData?)
data class ContentData(val parts: List<Part>?)

object GeminiService {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun improveResume(summary: String): String {
        return withContext(Dispatchers.IO) {
            try {

                if (apiKey.isEmpty() || apiKey == "PLACEHOLDER") {
                    return@withContext "Error: API key not configured"
                }

                val prompt = "Improve this resume summary. Make it professional, impactful, with action verbs, 2-3 sentences max.\n\nOriginal: \"$summary\"\n\nProvide ONLY improved text:"

                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(prompt)))
                    )
                )

                val jsonBody = gson.toJson(request)
                Log.d("GeminiService", "Starting resume enhancement request...")

                val httpRequest = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(httpRequest).execute()

                return@withContext if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "No response"
                    Log.d("GeminiService", "Enhancement successful")

                    val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                    geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                        ?: "Could not extract text"
                } else {
                    Log.e("GeminiService", "API Error: ${response.code}")
                    "Error: ${response.code}"
                }

            } catch (e: Exception) {
                Log.e("GeminiService", "Exception: ${e.javaClass.simpleName}")
                "Error: ${e.message}"
            }
        }
    }

    suspend fun improveExperience(jobTitle: String, description: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isEmpty() || apiKey == "PLACEHOLDER") {
                    return@withContext "Error: API key not configured"
                }

                val prompt = "Improve this job description. Use action verbs, highlight achievements, be quantifiable.\n\nTitle: $jobTitle\nDescription: $description\n\nProvide ONLY improved text:"

                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(prompt)))
                    )
                )

                val jsonBody = gson.toJson(request)
                Log.d("GeminiService", "Starting experience enhancement request...")

                val httpRequest = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(httpRequest).execute()

                return@withContext if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "No response"
                    Log.d("GeminiService", "Enhancement successful")

                    val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)
                    geminiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                        ?: "Could not extract text"
                } else {
                    Log.e("GeminiService", "API Error: ${response.code}")
                    "Error: ${response.code}"
                }

            } catch (e: Exception) {
                Log.e("GeminiService", "Exception: ${e.javaClass.simpleName}")
                "Error: ${e.message}"
            }
        }
    }
}
