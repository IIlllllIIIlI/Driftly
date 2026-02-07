package com.driftly.sleepsounds.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.driftly.sleepsounds.presentation.navigation.DriftlyNavGraph
import com.driftly.sleepsounds.presentation.navigation.Screen
import com.driftly.sleepsounds.presentation.theme.DriftlyTheme
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceVariant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op, permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        setContent {
            DriftlyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomNavItems = listOf(
                    BottomNavItem("Home", Screen.Home.route, Icons.Outlined.Home, Icons.Filled.Home),
                    BottomNavItem("Mixes", Screen.SavedMixes.route, Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
                    BottomNavItem("Settings", Screen.Settings.route, Icons.Outlined.Settings, Icons.Filled.Settings),
                )

                val showBottomBar = currentDestination?.hierarchy?.any { dest ->
                    bottomNavItems.any { it.route == dest.route }
                } == true

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(containerColor = SurfaceVariant) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == item.route
                                    } == true

                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.icon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = { Text(item.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Primary,
                                            selectedTextColor = Primary,
                                            indicatorColor = Surface,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    DriftlyNavGraph(
                        navController = navController,
                        onNavigateToPremium = {
                            navController.navigate(Screen.Premium.route)
                        }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)
