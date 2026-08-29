package com.example.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.auth.AuthViewModel
import com.example.ui.screens.*

@Composable
fun AppNavigation() {
  val navController = rememberNavController()
  val authViewModel: AuthViewModel = viewModel()

  NavHost(
    navController = navController,
    startDestination = "splash"
  ) {
    // 1. Splash Screen
    composable("splash") {
      SplashScreen(
        authViewModel = authViewModel,
        onNavigateToDashboard = {
          navController.navigate("dashboard") {
            popUpTo("splash") { inclusive = true }
          }
        },
        onNavigateToLogin = {
          navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
          }
        }
      )
    }

    // 2. Login Screen
    composable("login") {
      LoginScreen(
        authViewModel = authViewModel,
        onLoginSuccess = {
          navController.navigate("dashboard") {
            popUpTo("login") { inclusive = true }
          }
        },
        onNavigateToSignUp = {
          navController.navigate("signup")
        }
      )
    }
    
    // 2b. Sign Up Screen
    composable("signup") {
      SignUpScreen(
        authViewModel = authViewModel,
        onSignUpSuccess = {
          navController.navigate("dashboard") {
            popUpTo("login") { inclusive = true }
          }
        },
        onNavigateToLogin = {
          navController.popBackStack()
        }
      )
    }

    // 3. Home Dashboard Screen
    composable("dashboard") {
      DashboardScreen(
        authViewModel = authViewModel,
        onSubjectSelect = { subjectId ->
          navController.navigate("chapters/$subjectId")
        },
        onNavigateToProfile = {
          navController.navigate("profile")
        }
      )
    }

    // 4. Chapter List Screen
    composable(
      route = "chapters/{subjectId}",
      arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
    ) { backStackEntry ->
      val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
      ChapterListScreen(
        subjectId = subjectId,
        onNavigateBack = { navController.popBackStack() },
        onChapterSelect = { chapterId ->
          navController.navigate("chapterDetail/$subjectId/$chapterId")
        }
      )
    }

    // 5. Chapter Detail Screen (PDF Notes Reader + Quizzes)
    composable(
      route = "chapterDetail/{subjectId}/{chapterId}",
      arguments = listOf(
        navArgument("subjectId") { type = NavType.StringType },
        navArgument("chapterId") { type = NavType.StringType }
      )
    ) { backStackEntry ->
      val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
      val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
      ChapterDetailScreen(
        subjectId = subjectId,
        chapterId = chapterId,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 6. Student Profile Screen
    composable("profile") {
      ProfileScreen(
        authViewModel = authViewModel,
        onNavigateBack = { navController.popBackStack() },
        onLogout = {
          authViewModel.logout()
          navController.navigate("login") {
            popUpTo("dashboard") { inclusive = true }
          }
        }
      )
    }
  }
}
