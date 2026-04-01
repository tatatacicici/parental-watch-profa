package com.example.parental_watch.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.screens.ChildModeScreen
import com.example.parental_watch.ui.screens.HomeScreen
import com.example.parental_watch.ui.screens.ParentDashboardScreen
import com.example.parental_watch.ui.screens.ParentLoginScreen
import com.example.parental_watch.ui.screens.PinSetupScreen
import com.example.parental_watch.ui.theme.ParentalWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefManager = PreferencesManager(this)

        setContent {
            ParentalWatchTheme {
                AppNavigation(prefManager)
            }
        }
    }
}

@Composable
fun AppNavigation(prefManager: PreferencesManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onParentModeClick = {
                    if (prefManager.isPinSet()) {
                        navController.navigate(Routes.PARENT_LOGIN)
                    } else {
                        navController.navigate(Routes.PIN_SETUP)
                    }
                },
                onChildModeClick = {
                    navController.navigate(Routes.CHILD_MODE)
                }
            )
        }

        composable(Routes.PIN_SETUP) {
            PinSetupScreen(
                prefManager = prefManager,
                onPinSaved = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        // Hapus semua back stack agar tidak bisa back ke setup
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.PARENT_LOGIN) {
            ParentLoginScreen(
                prefManager = prefManager,
                onLoginSuccess = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                prefManager = prefManager,
                onLogout = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHILD_MODE) {
            ChildModeScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    val context = LocalContext.current
    val prefManager = PreferencesManager(context)
    ParentalWatchTheme {
        AppNavigation(prefManager = prefManager)
    }
}
