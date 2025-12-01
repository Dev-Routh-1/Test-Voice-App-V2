package com.tripandevent.sanbot.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tripandevent.sanbot.ui.screens.contact.ContactScreen
import com.tripandevent.sanbot.ui.screens.mainmenu.MainMenuScreen
import com.tripandevent.sanbot.ui.screens.media.MediaScreen
import com.tripandevent.sanbot.ui.screens.packagedetails.PackageDetailsScreen
import com.tripandevent.sanbot.ui.screens.packages.PackagesScreen
import com.tripandevent.sanbot.ui.screens.settings.SettingsScreen
import com.tripandevent.sanbot.ui.screens.thankyou.ThankYouScreen
import com.tripandevent.sanbot.ui.screens.voice.VoiceScreen
import com.tripandevent.sanbot.ui.screens.welcome.WelcomeScreen
import com.tripandevent.sanbot.utils.InactivityTimer
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object MainMenu : Screen("main_menu")
    object Voice : Screen("voice")
    object Packages : Screen("packages")
    object PackageDetails : Screen("package_details/{packageId}") {
        fun createRoute(packageId: String) = "package_details/$packageId"
    }
    object Contact : Screen("contact")
    object ThankYou : Screen("thank_you/{leadId}") {
        fun createRoute(leadId: String) = "thank_you/$leadId"
    }
    object Media : Screen("media")
    object Settings : Screen("settings")
}

@Composable
fun SanbotNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    val inactivityTimer = remember { InactivityTimer() }

    LaunchedEffect(Unit) {
        inactivityTimer.timeoutEvent.collectLatest {
            if (navController.currentDestination?.route != Screen.Welcome.route) {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(navController.currentDestination?.route) {
        val currentRoute = navController.currentDestination?.route
        if (currentRoute == Screen.Welcome.route) {
            inactivityTimer.stop()
        } else {
            inactivityTimer.start()
        }
    }

    fun resetInactivityTimer() {
        inactivityTimer.reset()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onStartClick = {
                    navController.navigate(Screen.MainMenu.route)
                }
            )
        }

        composable(Screen.MainMenu.route) {
            resetInactivityTimer()
            MainMenuScreen(
                onTalkToMeClick = {
                    resetInactivityTimer()
                    navController.navigate(Screen.Voice.route)
                },
                onBrowsePackagesClick = {
                    resetInactivityTimer()
                    navController.navigate(Screen.Packages.route)
                },
                onWatchVideosClick = {
                    resetInactivityTimer()
                    navController.navigate(Screen.Media.route)
                },
                onContactUsClick = {
                    resetInactivityTimer()
                    navController.navigate(Screen.Contact.route)
                },
                onSettingsClick = {
                    resetInactivityTimer()
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Voice.route) {
            resetInactivityTimer()
            VoiceScreen(
                onBackClick = {
                    resetInactivityTimer()
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Packages.route) {
            resetInactivityTimer()
            PackagesScreen(
                onBackClick = {
                    resetInactivityTimer()
                    navController.popBackStack()
                },
                onPackageClick = { packageId ->
                    resetInactivityTimer()
                    navController.navigate(Screen.PackageDetails.createRoute(packageId))
                }
            )
        }

        composable(
            route = Screen.PackageDetails.route,
            arguments = listOf(
                navArgument("packageId") { type = NavType.StringType }
            )
        ) {
            resetInactivityTimer()
            PackageDetailsScreen(
                onBackClick = {
                    resetInactivityTimer()
                    navController.popBackStack()
                },
                onBookNowClick = { _ ->
                    resetInactivityTimer()
                    navController.navigate(Screen.Contact.route)
                },
                onGetQuoteClick = { _ ->
                    resetInactivityTimer()
                    navController.navigate(Screen.Contact.route)
                }
            )
        }

        composable(Screen.Contact.route) {
            resetInactivityTimer()
            ContactScreen(
                onBackClick = {
                    resetInactivityTimer()
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onSuccess = { leadId ->
                    navController.navigate(Screen.ThankYou.createRoute(leadId)) {
                        popUpTo(Screen.MainMenu.route)
                    }
                }
            )
        }

        composable(
            route = Screen.ThankYou.route,
            arguments = listOf(
                navArgument("leadId") { type = NavType.StringType }
            )
        ) {
            resetInactivityTimer()
            ThankYouScreen(
                onHomeClick = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Media.route) {
            resetInactivityTimer()
            MediaScreen(
                onBackClick = {
                    resetInactivityTimer()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            resetInactivityTimer()
            SettingsScreen(
                onBackClick = {
                    resetInactivityTimer()
                    navController.popBackStack()
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            inactivityTimer.destroy()
        }
    }
}
