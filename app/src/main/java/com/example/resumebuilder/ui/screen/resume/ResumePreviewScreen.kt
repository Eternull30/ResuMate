package com.example.resumebuilder.ui.resume

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.resumebuilder.viewmodel.ProfileViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.resumebuilder.utils.PdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumePreviewScreen(
    uid: String,
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: NavController
) {

    val profile by viewModel.state.collectAsState()
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

        profile?.let { user ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                SectionTitle("Professional Summary")
                Text(user.bio)

                Divider()

                SectionTitle("Skills")
                Text(user.skills)

                Divider()

                SectionTitle("Experience")
                Text(user.experience)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (user.name.isBlank() || user.email.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please complete your profile first",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {

                            val uri = PdfGenerator.generate(context, user)

                            Toast.makeText(
                                context,
                                if (uri != null) "Saved to Downloads folder"
                                else "Failed to save PDF",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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


@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}
