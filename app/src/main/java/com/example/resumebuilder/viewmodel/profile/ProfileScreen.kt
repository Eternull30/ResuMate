package com.example.resumebuilder.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.resumebuilder.domain.model.UserProfile
import com.example.resumebuilder.viewmodel.ProfileViewModel
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uid: String,
    onNavigateToResume: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {

    val profile by viewModel.state.collectAsState()

    // Local editable states
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }

    // Load user once
    LaunchedEffect(uid) {
        viewModel.loadUser(uid)
    }

    // When profile changes → update textfields
    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            email = it.email
            bio = it.bio
            skills = it.skills
            experience = it.experience
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile") })
        }
    ) { paddingValues ->

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
                        UserProfile(
                            uid = uid,
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
}
