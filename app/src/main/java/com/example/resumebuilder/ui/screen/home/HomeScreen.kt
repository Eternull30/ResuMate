package com.example.resumebuilder.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.resumebuilder.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenResume: (String, String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {

    var expanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedResumeId by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }

    val resumes by viewModel.resumes.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("My Resumes") },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.AccountCircle, null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                expanded = false
                                onEditProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                expanded = false
                                onLogout()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createResume("New Resume", "modern")
                }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            items(
                items = resumes,
                key = { it.id }
            ) { resume ->

                val shape = RoundedCornerShape(24.dp)

                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                onOpenResume(resume.id, resume.templateType)
                            },
                            onLongClick = {
                                selectedResumeId = resume.id
                                showDeleteDialog = true
                            }
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF606060),   // top light
                                        Color(0xFF606060)    // bottom soft shadow
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {

                        Column {
                            Text(
                                text = resume.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF1E1E1E)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Template: ${resume.templateType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }

            }

        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameResume(selectedResumeId, newTitle)
                    showRenameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Rename Resume") },
            text = {
                TextField(
                    value = newTitle,
                    onValueChange = { newTitle = it }
                )
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteResume(selectedResumeId)
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Resume?") },
            text = { Text("This action cannot be undone.") }
        )
    }
}