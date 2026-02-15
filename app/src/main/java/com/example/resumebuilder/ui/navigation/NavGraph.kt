package com.example.resumebuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.resumebuilder.ui.profile.ProfileScreen
import com.example.resumebuilder.ui.resume.ResumePreviewScreen
import com.example.resumebuilder.viewmodel.ProfileViewModel

@Composable
fun NavGraph(
    startDestination: String = "profile"
) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // 🔹 Profile Screen
        composable("profile/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            ProfileScreen(
                uid = uid,
                onNavigateToResume = {
                    navController.navigate("resume/$uid")
                }
            )
        }

        composable("resume/{uid}") { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            ResumePreviewScreen(
                uid = uid,
                navController = navController
            )
        }


    }
}
