package com.example.resumebuilder.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.viewmodel.ProfileViewModel
import com.example.resumebuilder.ui.state.ProfileUiState
import com.example.resumebuilder.viewmodel.ProfileEvent
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    onNavigateToResume: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()


    LaunchedEffect(uid) {
        viewModel.loadUser(uid)
    }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ProfileEvent.NavigateToResume -> {
                    onNavigateToResume()
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { paddingValues ->

        when (uiState) {

            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(message)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadUser(uid) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {

                val profile = (uiState as ProfileUiState.Success).profile

                var name by remember(profile) { mutableStateOf(profile.name) }
                var email by remember(profile) { mutableStateOf(profile.email) }
                var bio by remember(profile) { mutableStateOf(profile.bio) }
                var skills by remember(profile) { mutableStateOf(profile.skills) }
                var experience by remember(profile) { mutableStateOf(profile.experience) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // Avatar Section
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = profile.name
                            .split(" ")
                            .take(2)
                            .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = initials.ifBlank { "U" },
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }


                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
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
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Professional Summary") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }

                    Text(
                        text = "Professional Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            OutlinedTextField(
                                value = skills,
                                onValueChange = { skills = it },
                                label = { Text("Skills (comma separated)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (skills.isNotBlank()) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    skills.split(",").map { it.trim() }.forEach { skill ->
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(skill) }
                                        )
                                    }
                                }
                            }


                            OutlinedTextField(
                                value = experience,
                                onValueChange = { experience = it },
                                label = { Text("Experience") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.save(
                                profile.copy(
                                    name = name,
                                    email = email,
                                    bio = bio,
                                    skills = skills,
                                    experience = experience,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Profile")
                        }
                    }


                    OutlinedButton(
                        onClick = onNavigateToResume,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preview Resume")
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }

            }
        }
    }
}

