package com.driftly.sleepsounds.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SavedMixes : Screen("saved_mixes")
    data object Settings : Screen("settings")
    data object Premium : Screen("premium")
}
