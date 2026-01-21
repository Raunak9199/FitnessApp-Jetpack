package com.nexusbihar.fitnessapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nexusbihar.fitnessapp.data.services.NotificationService
import com.nexusbihar.fitnessapp.ui.screens.AccessibilitySettingsScreen
import com.nexusbihar.fitnessapp.ui.screens.ConnectionStatusScreen
import com.nexusbihar.fitnessapp.ui.screens.GoalsScreen
import com.nexusbihar.fitnessapp.ui.screens.GoogleFitSetupScreen
import com.nexusbihar.fitnessapp.ui.screens.ModernDashboardScreen
import com.nexusbihar.fitnessapp.ui.screens.NotificationSettingsScreen
import com.nexusbihar.fitnessapp.ui.screens.ReportsScreen
import com.nexusbihar.fitnessapp.ui.screens.RunningMapScreen
import com.nexusbihar.fitnessapp.ui.screens.SettingsScreen
import com.nexusbihar.fitnessapp.ui.screens.SleepTrackingScreen
import com.nexusbihar.fitnessapp.ui.screens.StepTrackingScreen
import com.nexusbihar.fitnessapp.ui.screens.WaterTrackingScreen
import com.nexusbihar.fitnessapp.ui.screens.WearableSetupScreen

@Composable
fun HealthNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onSignOut: () -> Unit = {},
) {

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {

        composable("dashboard") {
            ModernDashboardScreen(
                onNavigateToWater = { navController.navigate("water") },
                onNavigateToSteps = { navController.navigate("steps") },
                onNavigateToSleep = { navController.navigate("sleep") },
                onNavigateToGoals = { navController.navigate("goals") },
                onNavigateToReports = { navController.navigate("reports") },
                onNavigateToConnectionStatus = { navController.navigate("connection_status") },
                onNavigateToRunningMap = { navController.navigate("running_map") },
                onSignOut = onSignOut
            )
        }

        composable("water") {
            WaterTrackingScreen()
        }

        composable("steps") {
            StepTrackingScreen()
        }

        composable("running_map") {
            RunningMapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("sleep") {
            SleepTrackingScreen()
        }

        composable("reports") {
            ReportsScreen()
        }

        composable("settings") {
            SettingsScreen(
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToGoals = { navController.navigate("goals") },
                onNavigateToAccessibility = { navController.navigate("accessibility") }
            )
        }

        composable("goals") {
            GoalsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("accessibility") {
            AccessibilitySettingsScreen(
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            // We'll need to inject NotificationService here
            // For now, let's create a simple version
            NotificationSettingsScreen(
                notificationService = NotificationService(
                    LocalContext.current
                ),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("connection_status") {
            ConnectionStatusScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGoogleFitSetup = { navController.navigate("google_fit_setup") },
                onNavigateToWearableSetup = { navController.navigate("wearable_setup") }
            )
        }

        composable("google_fit_setup") {
            GoogleFitSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetupComplete = { navController.popBackStack() }
            )
        }

        composable("wearable_setup") {
            WearableSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onSetupComplete = { navController.popBackStack() }
            )
        }

    }
}