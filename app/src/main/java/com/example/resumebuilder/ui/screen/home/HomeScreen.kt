package com.example.resumebuilder.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.resumebuilder.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenResume: (String, String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ResumeViewModel = hiltViewModel(),
    navController : NavController
) {

    var expanded by remember { mutableStateOf(false) }

    var showOptionsPopup by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedResumeId by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }

    val resumes by viewModel.resumes.collectAsState()

    Scaffold(
        containerColor = Color(0xFF121212),
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

                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(resume.id) {
                    visible = false
                    visible = true
                }

                val scale by animateFloatAsState(
                    targetValue = if (visible) 1f else 0.85f,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 300f
                    ),
                    label = "cardScale"
                )

                val alpha by animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = tween(300),
                    label = "cardAlpha"
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1F1F1F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .combinedClickable(
                            onClick = {
                                navController.navigate("editor/${resume.id}")
                            },
                            onLongClick = {
                                selectedResumeId = resume.id
                                newTitle = resume.title
                                showOptionsPopup = true
                            }
                        )

                ) {

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = resume.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF5F5F5)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Template: ${resume.templateType}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                }
            }

        }
    }

    // ---------------- OPTIONS POPUP ----------------

    if (showOptionsPopup) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { showOptionsPopup = false },
            contentAlignment = Alignment.Center
        ) {

            val transition = updateTransition(
                targetState = showOptionsPopup,
                label = "popupTransition"
            )

            val scale by transition.animateFloat(
                transitionSpec = {
                    spring(
                        dampingRatio = 0.7f,
                        stiffness = 400f
                    )
                },
                label = "scaleAnim"
            ) { visible ->
                if (visible) 1f else 0.6f
            }

            val alpha by transition.animateFloat(
                transitionSpec = { tween(220) },
                label = "alphaAnim"
            ) { visible ->
                if (visible) 1f else 0f
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                modifier = Modifier
                    .padding(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Button(
                        onClick = {
                            showOptionsPopup = false
                            showRenameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rename")
                    }

                    Button(
                        onClick = {
                            showOptionsPopup = false
                            showDeleteDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }


    // ---------------- RENAME DIALOG ----------------

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
                    onValueChange = { newTitle = it },
                    singleLine = true
                )
            }
        )
    }

    // ---------------- DELETE DIALOG ----------------

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
