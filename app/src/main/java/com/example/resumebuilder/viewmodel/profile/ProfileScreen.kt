package com.example.resumebuilder.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
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

    LaunchedEffect(uid) {
        viewModel.loadUser(uid)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = { Text("Skills (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text("Experience") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile")
                    }

                    Button(
                        onClick = onNavigateToResume,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Preview Resume")
                    }
                }
            }


            is ProfileUiState.SaveSuccess -> {

                LaunchedEffect(Unit) {
                    viewModel.event.collect { event ->
                        when (event) {
                            is ProfileEvent.NavigateToResume -> {
                                onNavigateToResume()
                            }
                        }
                    }
                }


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

