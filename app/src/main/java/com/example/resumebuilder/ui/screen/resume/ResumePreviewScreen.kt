package com.example.resumebuilder.ui.resume

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.resumebuilder.viewmodel.ProfileViewModel
import com.example.resumebuilder.ui.state.ProfileUiState
import com.example.resumebuilder.utils.PdfGenerator
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumePreviewScreen(
    uid: String,
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uid) {
        viewModel.loadUser(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Preview") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        when (uiState) {

            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileUiState.Error -> {
                val message = (uiState as ProfileUiState.Error).message

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadUser(uid) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {

                val user = (uiState as ProfileUiState.Success).profile

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.TopCenter
                ) {

                    Column(
                        modifier = Modifier
                            .width(400.dp) // A4-like width
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 24.dp)
                    ) {

                        Card(
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(32.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {

                                // Header
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Divider()

                                ResumeSection(
                                    title = "Professional Summary",
                                    content = user.bio
                                )

                                ResumeSection(
                                    title = "Skills",
                                    content = user.skills
                                )

                                ResumeSection(
                                    title = "Experience",
                                    content = user.experience
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                val uri = PdfGenerator.generate(context, user)

                                Toast.makeText(
                                    context,
                                    if (uri != null)
                                        "Saved to Downloads folder"
                                    else
                                        "Failed to save PDF",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Download as PDF")
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }


        }

    }
}

@Composable
private fun ResumeSection(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

