package com.driftly.sleepsounds.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DriftlyColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    secondary = PrimaryVariant,
    onSecondary = OnBackground,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurface,
    error = Error,
    onError = OnBackground,
)

@Composable
fun DriftlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DriftlyColorScheme,
        content = content
    )
}
