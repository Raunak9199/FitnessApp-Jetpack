package com.nexusbihar.fitnessapp.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexusbihar.fitnessapp.ui.components.MultiOptionFAB
import com.nexusbihar.fitnessapp.ui.navigation.BottomNavigationBar
import com.nexusbihar.fitnessapp.ui.navigation.HealthNavigation
import com.nexusbihar.fitnessapp.ui.screens.AuthScreen
import com.nexusbihar.fitnessapp.ui.screens.SplashScreen
import com.nexusbihar.fitnessapp.ui.theme.FitnessAppTheme
import com.nexusbihar.fitnessapp.ui.viewmodel.AuthViewModel
import com.nexusbihar.fitnessapp.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)


        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val isHighContrast by themeViewModel.isHighContrast.collectAsState()

            FitnessAppTheme(
                darkTheme = isDarkTheme,
                highContrast = isHighContrast
            ) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val user by authViewModel.user.collectAsState()

                var showSplash by remember { mutableStateOf(true) }

                when {
                    showSplash -> {
                        SplashScreen(
                            onSplashFinished = {
                                showSplash = false
                            }
                        )
                    }

                    user != null -> {
                        // User is authenticated, show main app
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                BottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        if (currentRoute != route) {
                                            if (route == "dashboard") {
                                                // For home navigation, clear back stack and go to dashboard
                                                navController.navigate(route) {
                                                    popUpTo(0) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                // For other screens, use normal navigation
                                                navController.navigate(route) {
                                                    popUpTo("dashboard") {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                )
                            },
                            floatingActionButton = {
                                MultiOptionFAB(
                                    onWaterClick = {
                                        if (currentRoute != "water") {
                                            navController.navigate("water") {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    onStepsClick = {
                                        if (currentRoute != "steps") {
                                            navController.navigate("steps") {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    onSleepClick = {
                                        if (currentRoute != "sleep") {
                                            navController.navigate("sleep") {
                                                popUpTo("dashboard") {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            val layoutDirection = LocalLayoutDirection.current
                            HealthNavigation(
                                navController = navController,
                                onSignOut = { authViewModel.signOut() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .statusBarsPadding()
                            )
                        }
                    }

                    else -> {
                        // User is not authenticated, show auth screen
                        AuthScreen(
                            onAuthSuccess = {
                                // Auth success is handled by the LaunchedEffect in AuthScreen
                            }
                        )
                    }
                }
            }
        }
    }
}