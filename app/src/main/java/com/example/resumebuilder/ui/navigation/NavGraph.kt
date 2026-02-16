package com.example.resumebuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.resumebuilder.ui.profile.ProfileScreen
import com.example.resumebuilder.ui.resume.ResumePreviewScreen
import com.example.resumebuilder.ui.auth.LoginScreen
import com.example.resumebuilder.ui.auth.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {

    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination = if (currentUser != null) "profile" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("login") {
            LoginScreen(navController)
        }

        composable("profile") {
            ProfileScreen(
                uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                onNavigateToResume = {
                    navController.navigate("resume")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )

        }

        composable("resume") {
            ResumePreviewScreen(
                uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                navController = navController
            )
        }
        composable("register") {
            RegisterScreen(navController)
        }

        composable("profile") {
            ProfileScreen(
                uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                onNavigateToResume = {
                    navController.navigate("resume")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }


    }
}
