package com.driftly.sleepsounds.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.driftly.sleepsounds.presentation.screens.home.HomeScreen
import com.driftly.sleepsounds.presentation.screens.premium.PremiumScreen
import com.driftly.sleepsounds.presentation.screens.savedmixes.SavedMixesScreen
import com.driftly.sleepsounds.presentation.screens.settings.SettingsScreen

@Composable
fun DriftlyNavGraph(
    navController: NavHostController,
    onNavigateToPremium: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onNavigateToPremium = onNavigateToPremium)
        }
        composable(Screen.SavedMixes.route) {
            SavedMixesScreen(onNavigateToPremium = onNavigateToPremium)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateToPremium = onNavigateToPremium)
        }
        composable(Screen.Premium.route) {
            PremiumScreen(onBack = { navController.popBackStack() })
        }
    }
}
