package com.example.resumebuilder.ui.screen.resume

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.resumebuilder.utils.PdfGenerator
import com.example.resumebuilder.viewmodel.ResumeViewModel
import com.example.resumebuilder.data.remote.gemini.GeminiService
import com.example.resumebuilder.domain.model.Resume
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeEditorScreen(
    resumeId: String,
    viewModel: ResumeViewModel,
    navController: NavController
) {
    // Get context locally
    val context = LocalContext.current

    val resumes by viewModel.resumes.collectAsState()
    val resume = resumes.find { it.id == resumeId }

    if (resume == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var fullName by remember { mutableStateOf(resume.fullName) }
    var email by remember { mutableStateOf(resume.email) }
    var phone by remember { mutableStateOf(resume.phone) }
    var summary by remember { mutableStateOf(resume.summary) }
    var skills by remember { mutableStateOf(resume.skills.joinToString(", ")) }
    var experience by remember { mutableStateOf(resume.experience.joinToString("\n")) }

    val scope = rememberCoroutineScope()
    var isEnhancing by remember { mutableStateOf(false) }
    var enhancedSummary by remember { mutableStateOf<String?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Resume",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("Professional Summary") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ FIXED: Keep ONLY this enhance button
            Button(
                onClick = {
                    isEnhancing = true
                    scope.launch {
                        val improved = GeminiService.improveResume(summary)
                        enhancedSummary = improved
                        isEnhancing = false
                    }
                },
                enabled = summary.isNotEmpty() && !isEnhancing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isEnhancing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enhancing...")
                } else {
                    Text("✨ Enhance with AI")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show enhanced version if available
            if (enhancedSummary != null) {
                Text(
                    "Enhanced Version:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        enhancedSummary!!,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Use enhanced version
                    Button(
                        onClick = {
                            summary = enhancedSummary!!
                            enhancedSummary = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use This")
                    }

                    // Discard enhancement
                    Button(
                        onClick = {
                            enhancedSummary = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("Discard")
                    }
                }
            }

            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Skills (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Experience") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            // Save Resume Button
            Button(
                onClick = {
                    val skillsList = skills
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val experienceList = experience
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    viewModel.updateResume(
                        resume.copy(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            summary = summary,
                            skills = skillsList,
                            experience = experienceList
                        )
                    )

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Resume")
            }

            // Download PDF Button
            Button(
                onClick = {
                    val skillsList = skills
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val experienceList = experience
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    val updatedResume = resume.copy(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        summary = summary,
                        skills = skillsList,
                        experience = experienceList
                    )

                    val success = PdfGenerator.generateResumePdf(
                        context = context,
                        resume = updatedResume
                    )

                    if (success) {
                        Toast.makeText(
                            context,
                            "Saved to Downloads/ResumeBuilder",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to save PDF",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Download PDF")
            }
        }
    }
}