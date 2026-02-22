package com.example.resumebuilder.data.remote.gemini

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

fun main() {
    val apiKey = "AIzaSyBhvmee1K_v0FtlkGlQrX2OJ3q7bsYyC7w"

    val client = OkHttpClient()

    val json = """
    {
        "contents": [
            {
                "parts": [
                    {
                        "text": "Say hello"
                    }
                ]
            }
        ]
    }
    """.trimIndent()

    val body = json.toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=$apiKey")
        .post(body)
        .build()

    try {
        val response = client.newCall(request).execute()
        println("Status Code: ${response.code}")
        println("Response: ${response.body?.string()}")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}