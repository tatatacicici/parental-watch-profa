package com.example.parental_watch.ui

import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parental_watch.data.preference.PreferencesManager
import com.example.parental_watch.ui.dashboard.VideoHistoryScreen
import com.example.parental_watch.ui.player.BlockScreen
import com.example.parental_watch.ui.player.PlayerScreen
import com.example.parental_watch.ui.player.VideoCheckScreen
import com.example.parental_watch.ui.screens.ChangePinScreen
import com.example.parental_watch.ui.screens.SplashScreen
import com.example.parental_watch.ui.screens.ForgotPasswordScreen
import com.example.parental_watch.ui.screens.GoogleLoginScreen
import com.example.parental_watch.ui.screens.HomeScreen
import com.example.parental_watch.ui.screens.LogScreen
import com.example.parental_watch.ui.screens.OnboardingScreen
import com.example.parental_watch.ui.screens.ParentDashboardScreen
import com.example.parental_watch.ui.screens.ParentLoginScreen
import com.example.parental_watch.ui.screens.PermissionScreen
import com.example.parental_watch.ui.screens.PinSetupScreen
import com.example.parental_watch.ui.screens.StudyScheduleScreen
import com.example.parental_watch.ui.screens.WhitelistScreen
import com.example.parental_watch.ui.search.SearchScreen
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

    val startDestination = Routes.SPLASH

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Splash Screen ─────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    val next = when {
                        !prefManager.isGoogleLoggedIn() -> Routes.GOOGLE_LOGIN
                        !prefManager.isOnboardingComplete() -> Routes.ONBOARDING
                        else -> Routes.HOME
                    }
                    navController.navigate(next) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Google Login ─────────────────────────────────────
        composable(Routes.GOOGLE_LOGIN) {
            GoogleLoginScreen(
                prefManager = prefManager,
                onLoginSuccess = {
                    val next = if (prefManager.isOnboardingComplete()) {
                        Routes.HOME
                    } else {
                        Routes.ONBOARDING
                    }
                    navController.navigate(next) {
                        popUpTo(Routes.GOOGLE_LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding Tutorial ─────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    prefManager.setOnboardingComplete(true)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

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
                },
                onForgotPasswordClick = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        // ── Forgot Password ───────────────────────────────────
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                prefManager = prefManager,
                onBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Routes.PARENT_LOGIN) {
                        popUpTo(Routes.FORGOT_PASSWORD) { inclusive = true }
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
                onChangePinClick = {
                    navController.navigate(Routes.CHANGE_PIN)
                },
                onVideoHistoryClick = {
                    navController.navigate(Routes.VIDEO_HISTORY)
                },
                onStudyScheduleClick = {
                    navController.navigate(Routes.STUDY_SCHEDULE)
                },
                onGoogleLogoutClick = {
                    // Clear Google Login state
                    prefManager.setGoogleLoggedIn(false)
                    // Clear WebView cookies/session to force re-login next time
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    WebStorage.getInstance().deleteAllData()
                    
                    // Navigate to Google Login
                    navController.navigate(Routes.GOOGLE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Child Mode ────────────────────────────────────────
        composable(Routes.CHILD_MODE) {
            SearchScreen(
                isStudyTime = prefManager.isStudyTimeNow(),
                isTimeTampered = prefManager.isTimeTampered(),
                studyScheduleText = prefManager.getStudyScheduleText(),
                onVideoSelected = { video ->
                    navController.navigate(
                        Routes.videoCheck(
                            videoId = video.videoId,
                            title = video.title,
                            channelTitle = video.channelTitle,
                            thumbnailUrl = video.thumbnailUrl
                        )
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VIDEO_CHECK,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("channelTitle") { type = NavType.StringType },
                navArgument("thumbnailUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            if (prefManager.isStudyTimeNow()) {
                SearchScreen(
                    isStudyTime = true,
                    isTimeTampered = prefManager.isTimeTampered(),
                    studyScheduleText = prefManager.getStudyScheduleText(),
                    onVideoSelected = {},
                    onBack = { navController.popBackStack() }
                )
            } else {
                val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                val title = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("title") ?: "", "UTF-8"
                )
                val channelTitle = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("channelTitle") ?: "", "UTF-8"
                )
                val thumbnailUrl = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("thumbnailUrl") ?: "", "UTF-8"
                )
                VideoCheckScreen(
                    videoId = videoId,
                    title = title,
                    channelTitle = channelTitle,
                    thumbnailUrl = thumbnailUrl,
                    onApproved = { vid, ttl ->
                        navController.navigate(Routes.player(vid, ttl)) {
                            popUpTo(Routes.VIDEO_CHECK) { inclusive = true }
                        }
                    },
                    onBlocked = { vid, ttl, reason, ratio ->
                        navController.navigate(Routes.block(vid, ttl, reason, ratio)) {
                            popUpTo(Routes.VIDEO_CHECK) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            val title = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "", "UTF-8"
            )
            PlayerScreen(
                videoId = videoId,
                title = title,
                onRelatedVideoClicked = { relatedVideoId ->
                    navController.navigate(
                        Routes.videoCheck(
                            videoId = relatedVideoId,
                            title = "",
                            channelTitle = "",
                            thumbnailUrl = ""
                        )
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.BLOCK,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("reason") { type = NavType.StringType },
                navArgument("ratio") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            val title = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "", "UTF-8"
            )
            // reason di-doubleEncode di Routes.block() karena mengandung '%'.
            // NavController sudah Uri.decode sekali → kita cukup URLDecoder.decode sekali lagi.
            val reason = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("reason") ?: "", "UTF-8"
            )
            val ratio = backStackEntry.arguments?.getFloat("ratio") ?: 0f
            BlockScreen(
                videoId = videoId,
                title = title,
                reason = reason,
                ratio = ratio,
                onRecommendationSelected = { video ->
                    navController.navigate(
                        Routes.videoCheck(
                            videoId = video.videoId,
                            title = video.title,
                            channelTitle = video.channelTitle,
                            thumbnailUrl = video.thumbnailUrl
                        )
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VIDEO_HISTORY) {
            VideoHistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.STUDY_SCHEDULE) {
            StudyScheduleScreen(
                prefManager = prefManager,
                onBack = { navController.popBackStack() }
            )
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
