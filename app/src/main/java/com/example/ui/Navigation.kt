package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*

@Composable
fun AppNavigation() {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = "splash"
  ) {
    // 1. Splash Screen
    composable("splash") {
      SplashScreen(
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
        onLoginSuccess = {
          navController.navigate("dashboard") {
            popUpTo("login") { inclusive = true }
          }
        }
      )
    }

    // 3. Home Dashboard Screen
    composable("dashboard") {
      DashboardScreen(
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
        onNavigateBack = { navController.popBackStack() },
        onLogout = {
          navController.navigate("login") {
            popUpTo("dashboard") { inclusive = true }
          }
        }
      )
    }
  }
}
