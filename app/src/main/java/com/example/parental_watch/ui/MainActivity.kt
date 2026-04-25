package com.example.parental_watch.ui

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.screens.ChangePinScreen
import com.example.parental_watch.ui.screens.ChildModeScreen
import com.example.parental_watch.ui.screens.HomeScreen
import com.example.parental_watch.ui.screens.LogScreen
import com.example.parental_watch.ui.screens.ParentDashboardScreen
import com.example.parental_watch.ui.screens.ParentLoginScreen
import com.example.parental_watch.ui.screens.PermissionScreen
import com.example.parental_watch.ui.screens.PinSetupScreen
import com.example.parental_watch.ui.screens.WhitelistScreen
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
        // ── Home ─────────────────────────────────────────────
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

        // ── Permission ────────────────────────────────────────
        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Routes.PARENT_DASHBOARD) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // ── PIN Setup ─────────────────────────────────────────
        composable(Routes.PIN_SETUP) {
            PinSetupScreen(
                prefManager = prefManager,
                onPinSaved = {
                    // Setelah setup PIN, cek permission overlay dulu
                    navController.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // ── Parent Login ──────────────────────────────────────
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

        // ── Parent Dashboard ──────────────────────────────────
        composable(Routes.PARENT_DASHBOARD) {
            ParentDashboardScreen(
                prefManager = prefManager,
                onLogout = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onWhitelistClick = {
                    navController.navigate(Routes.WHITELIST)
                },
                onLogClick = {
                    navController.navigate(Routes.LOG)
                },
                onChangePinClick = {
                    navController.navigate(Routes.CHANGE_PIN)
                }
            )
        }

        // ── Child Mode ────────────────────────────────────────
        composable(Routes.CHILD_MODE) {
            ChildModeScreen()
        }

        // ── Whitelist ─────────────────────────────────────────
        composable(Routes.WHITELIST) {
            WhitelistScreen(
                prefManager = prefManager,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Log ───────────────────────────────────────────────
        composable(Routes.LOG) {
            LogScreen(onBack = { navController.popBackStack() })
        }

        // ── Change PIN ────────────────────────────────────────
        composable(Routes.CHANGE_PIN) {
            ChangePinScreen(
                prefManager = prefManager,
                onBack = { navController.popBackStack() }
            )
        }
    }
}