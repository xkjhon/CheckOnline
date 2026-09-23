package br.com.meirelesefreitas.go.checkonline.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.meirelesefreitas.go.checkonline.ui.screens.MainAppScreen
import br.com.meirelesefreitas.go.checkonline.ui.screens.auth.FirstAccessScreen
import br.com.meirelesefreitas.go.checkonline.ui.screens.auth.LoginScreen
import br.com.meirelesefreitas.go.checkonline.ui.screens.checklist.ChecklistScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToFirstAccess = { matricula, mode ->
                    navController.navigate(Screen.FirstAccess.createRoute(matricula, mode))
                }
            )
        }

        // First Access / Reset Password Screen
        composable(
            route = Screen.FirstAccess.route,
            arguments = listOf(
                navArgument("matricula") { type = NavType.StringType },
                navArgument("mode") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val mode = backStackEntry.arguments?.getLong("mode") ?: 1L

            FirstAccessScreen(
                matricula = matricula,
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main App Screen (Home, History, Profile Tabs)
        composable(Screen.Main.route) {
            MainAppScreen(
                onNavigateToChecklist = {
                    navController.navigate(Screen.Checklist.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // Checklist Screen
        composable(
            route = Screen.Checklist.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(350)
                )
            }
        ) {
            ChecklistScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubmissionSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
