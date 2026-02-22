package com.example.resumebuilder.data.remote.gemini

import android.util.Log
import com.example.resumebuilder.BuildConfig
import com.example.resumebuilder.domain.model.Resume
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Data classes for Gemini API
data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)
data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: ContentData?)
data class ContentData(val parts: List<Part>?)

object GeminiService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)  // Use custom client with longer timeout
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geminiApi = retrofit.create(GeminiApi::class.java)
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val gson = Gson()

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

                Log.d("GeminiService", "Starting single field enhancement...")

                val response = geminiApi.generateContent(apiKey, request)

                return@withContext if (response.candidates != null && response.candidates.isNotEmpty()) {
                    val improvedText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                    Log.d("GeminiService", "Enhancement successful: ${improvedText?.take(50)}...")
                    improvedText ?: "Could not extract text"
                } else {
                    Log.e("GeminiService", "No candidates in response")
                    "Error: No response from Gemini"
                }

            } catch (e: Exception) {
                Log.e("GeminiService", "Exception: ${e.message}", e)
                "Error: ${e.message}"
            }
        }
    }

    suspend fun improveAllResumeData(resume: Resume): Resume {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isEmpty() || apiKey == "PLACEHOLDER") {
                    Log.e("GeminiService", "API key not configured")
                    return@withContext resume
                }

                Log.d("GeminiService", "Starting full resume improvement via Retrofit...")

                val prompt = """
                    Improve this resume to be more professional and impactful. Enhance each section with action verbs and quantifiable achievements.
                    
                    Return the response in EXACTLY this format (with these exact bracket markers):
                    
                    [PROFESSIONAL SUMMARY]
                    ${resume.summary.ifEmpty { "No summary provided" }}
                    
                    [EDUCATION]
                    ${resume.education.joinToString("\n").ifEmpty { "No education provided" }}
                    
                    [TECHNICAL SKILLS]
                    ${resume.skills.joinToString(", ").ifEmpty { "No skills provided" }}
                    
                    [RELEVANT EXPERIENCE]
                    ${resume.experience.joinToString("\n").ifEmpty { "No experience provided" }}
                    
                    [PROJECTS]
                    ${resume.projects.joinToString("\n").ifEmpty { "No projects provided" }}
                    
                    Now improve each section. Return in this exact format:
                    
                    [PROFESSIONAL SUMMARY]
                    [Your improved summary here - make it powerful and concise]
                    
                    [EDUCATION]
                    [Line 1 improved]
                    [Line 2 improved]
                    
                    [TECHNICAL SKILLS]
                    [Skill1, Skill2, Skill3 - improved and enhanced]
                    
                    [RELEVANT EXPERIENCE]
                    [Experience 1 improved with action verbs and metrics]
                    [Experience 2 improved with action verbs and metrics]
                    
                    [PROJECTS]
                    [Project 1 improved description]
                    [Project 2 improved description]
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(prompt)))
                    )
                )

                Log.d("GeminiService", "Sending Retrofit request to Gemini API...")

                val response = geminiApi.generateContent(apiKey, request)

                return@withContext if (response.candidates != null && response.candidates.isNotEmpty()) {
                    val improvedText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()

                    if (improvedText.isNullOrEmpty()) {
                        Log.e("GeminiService", "Response is empty")
                        return@withContext resume
                    }

                    Log.d("GeminiService", "Response received. Response length: ${improvedText.length}")
                    Log.d("GeminiService", "Full response: $improvedText")

                    // Parse the response
                    val improvedResume = parseImprovedResume(improvedText, resume)
                    Log.d("GeminiService", "Parsing complete - Summary updated: ${improvedResume.summary.take(50)}...")

                    improvedResume

                } else {
                    Log.e("GeminiService", "No candidates in response")
                    resume
                }

            } catch (e: Exception) {
                Log.e("GeminiService", "Exception during improvement: ${e.message}", e)
                e.printStackTrace()
                resume
            }
        }
    }

    private fun parseImprovedResume(text: String, original: Resume): Resume {
        return try {
            Log.d("GeminiService", "Starting to parse response...")

            val summarySection = extractSection(text, "[PROFESSIONAL SUMMARY]", "[EDUCATION]").trim()
            val educationSection = extractSection(text, "[EDUCATION]", "[TECHNICAL SKILLS]").trim()
            val skillsSection = extractSection(text, "[TECHNICAL SKILLS]", "[RELEVANT EXPERIENCE]").trim()
            val experienceSection = extractSection(text, "[RELEVANT EXPERIENCE]", "[PROJECTS]").trim()
            val projectsSection = extractSection(text, "[PROJECTS]", "").trim()

            Log.d("GeminiService", "Summary extracted: ${summarySection.take(50)}...")
            Log.d("GeminiService", "Education extracted: ${educationSection.take(50)}...")
            Log.d("GeminiService", "Skills extracted: ${skillsSection.take(50)}...")
            Log.d("GeminiService", "Experience extracted: ${experienceSection.take(50)}...")
            Log.d("GeminiService", "Projects extracted: ${projectsSection.take(50)}...")

            Resume(
                id = original.id,
                title = original.title,
                templateType = original.templateType,
                createdAt = original.createdAt,
                fullName = original.fullName,
                email = original.email,
                phone = original.phone,
                summary = summarySection.ifEmpty { original.summary },
                education = educationSection.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { original.education },
                skills = skillsSection.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { original.skills },
                experience = experienceSection.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { original.experience },
                projects = projectsSection.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .ifEmpty { original.projects }
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error parsing response: ${e.message}", e)
            original
        }
    }
    private fun extractSection(text: String, startMarker: String, endMarker: String): String {
        return try {
            val startIndex = text.indexOf(startMarker)
            if (startIndex == -1) {
                Log.w("GeminiService", "Start marker not found: $startMarker")
                return ""
            }

            val contentStart = startIndex + startMarker.length

            val endIndex = if (endMarker.isEmpty()) {
                text.length
            } else {
                val foundEnd = text.indexOf(endMarker, contentStart)
                if (foundEnd == -1) text.length else foundEnd
            }

            text.substring(contentStart, endIndex).trim()
        } catch (e: Exception) {
            Log.e("GeminiService", "Error extracting section: ${e.message}")
            ""
        }
    }
}
