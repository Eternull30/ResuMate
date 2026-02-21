package com.example.resumebuilder.data.remote.gemini

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() = runBlocking {
    val apiKey = "YOUR_API_KEY"

    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1/models?key=$apiKey")
        .get()
        .build()

    try {
        val response = client.newCall(request).execute()
        println("Status: ${response.code}")
        println("Models:")
        println(response.body?.string())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}