package com.example.resumebuilder.ui.screen.resume

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.resumebuilder.utils.PdfGenerator
import com.example.resumebuilder.viewmodel.ResumeViewModel
import com.example.resumebuilder.data.remote.gemini.GeminiService
import com.example.resumebuilder.domain.model.Resume
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeEditorScreen(
    resumeId: String,
    viewModel: ResumeViewModel,
    navController: NavController
) {
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
    var education by remember { mutableStateOf(resume.education.joinToString("\n")) }
    var skills by remember { mutableStateOf(resume.skills.joinToString(", ")) }
    var experience by remember { mutableStateOf(resume.experience.joinToString("\n")) }
    var projects by remember { mutableStateOf(resume.projects.joinToString("\n")) }
    var templateType by remember { mutableStateOf(resume.templateType) }
    var isImproving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Resume") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Select Template:", style = MaterialTheme.typography.titleSmall)
            var templateExpanded by remember { mutableStateOf(false) }

            Box {
                OutlinedButton(
                    onClick = { templateExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(templateType.replaceFirstChar { it.uppercase() })
                }
                DropdownMenu(
                    expanded = templateExpanded,
                    onDismissRequest = { templateExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("modern", "professional", "creative").forEach { template ->
                        DropdownMenuItem(
                            text = { Text(template.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                templateType = template
                                templateExpanded = false
                            }
                        )
                    }
                }
            }

            Divider()

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = summary,
                onValueChange = { summary = it },
                label = { Text("Professional Summary") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("Education (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Technical Skills (comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Experience (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = projects,
                onValueChange = { projects = it },
                label = { Text("Projects (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Divider()

            Button(
                onClick = {
                    scope.launch {
                        isImproving = true
                        Log.d("ResumeEditor", "=== IMPROVE BUTTON CLICKED ===")

                        try {
                            if (summary.isEmpty() && skills.isEmpty() && experience.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please fill in at least one field",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w("ResumeEditor", "No fields filled")
                                isImproving = false
                                return@launch
                            }

                            Log.d("ResumeEditor", "Summary before: $summary")
                            Log.d("ResumeEditor", "Skills before: $skills")
                            Log.d("ResumeEditor", "Experience before: $experience")

                            val educationList = education.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                            val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val experienceList = experience.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                            val projectsList = projects.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

                            val currentResume = Resume(
                                id = resume.id,
                                title = resume.title,
                                templateType = templateType,
                                createdAt = resume.createdAt,
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                summary = summary,
                                education = educationList,
                                skills = skillsList,
                                experience = experienceList,
                                projects = projectsList
                            )

                            Log.d("ResumeEditor", "Calling GeminiService.improveAllResumeData...")
                            val improvedResume = GeminiService.improveAllResumeData(currentResume)
                            Log.d("ResumeEditor", "Got response from Gemini")

                            Log.d("ResumeEditor", "Summary after: ${improvedResume.summary}")
                            Log.d("ResumeEditor", "Skills after: ${improvedResume.skills}")
                            Log.d("ResumeEditor", "Experience after: ${improvedResume.experience}")

                            summary = improvedResume.summary
                            education = improvedResume.education.joinToString("\n")
                            skills = improvedResume.skills.joinToString(", ")
                            experience = improvedResume.experience.joinToString("\n")
                            projects = improvedResume.projects.joinToString("\n")

                            Log.d("ResumeEditor", "Fields updated in UI")
                            Toast.makeText(context, "✨ Resume improved!", Toast.LENGTH_SHORT).show()

                        } catch (e: Exception) {
                            Log.e("ResumeEditor", "Exception: ${e.message}", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isImproving = false
                            Log.d("ResumeEditor", "=== IMPROVE COMPLETE ===")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImproving
            ) {
                if (isImproving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Improving...")
                } else {
                    Text("✨ Improve All with AI")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val educationList = education.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val experienceList = experience.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val projectsList = projects.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

                    viewModel.updateResume(
                        resume.copy(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            summary = summary,
                            education = educationList,
                            skills = skillsList,
                            experience = experienceList,
                            projects = projectsList,
                            templateType = templateType
                        )
                    )

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Resume")
            }

            Button(
                onClick = {
                    val educationList = education.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val skillsList = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val experienceList = experience.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val projectsList = projects.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

                    val updatedResume = resume.copy(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        summary = summary,
                        education = educationList,
                        skills = skillsList,
                        experience = experienceList,
                        projects = projectsList,
                        templateType = templateType
                    )

                    val success = PdfGenerator.generateResumePdf(context, updatedResume)

                    if (success) {
                        Toast.makeText(context, "Saved to Downloads/ResumeBuilder", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("📥 Download PDF")
            }
        }
    }
}
