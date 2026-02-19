package com.example.resumebuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.resumebuilder.ui.auth.LoginScreen
import com.example.resumebuilder.ui.auth.RegisterScreen
import com.example.resumebuilder.ui.profile.ProfileScreen
import com.example.resumebuilder.ui.resume.ResumePreviewScreen
import com.example.resumebuilder.ui.screen.home.HomeScreen
import com.example.resumebuilder.ui.screen.resume.ResumeEditorScreen
import com.example.resumebuilder.viewmodel.ResumeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {

    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val resumeViewModel: ResumeViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "home" else "login"
    ) {


        composable("login") {
            LoginScreen(navController)
        }


        composable("register") {
            RegisterScreen(navController)
        }


        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = resumeViewModel,
                onOpenResume = { resumeId, template ->

                    navController.navigate("editor/$resumeId")
                },
                onEditProfile = {
                    navController.navigate("profile")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }


        composable("profile") {
            ProfileScreen(
                uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                onNavigateToResume = {
                    navController.navigate("home")
                },
                onBack = { navController.popBackStack() },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }


        composable(
            route = "editor/{resumeId}",
            arguments = listOf(
                navArgument("resumeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val resumeId =
                backStackEntry.arguments?.getString("resumeId") ?: ""

            ResumeEditorScreen(
                resumeId = resumeId,
                viewModel = resumeViewModel,
                navController = navController
            )
        }


        composable(
            route = "resume/{resumeId}/{template}",
            arguments = listOf(
                navArgument("resumeId") { type = NavType.StringType },
                navArgument("template") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val resumeId =
                backStackEntry.arguments?.getString("resumeId") ?: ""

            val template =
                backStackEntry.arguments?.getString("template") ?: "modern"

            ResumePreviewScreen(
                uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                resumeId = resumeId,
                template = template,
                navController = navController
            )
        }
    }
}
