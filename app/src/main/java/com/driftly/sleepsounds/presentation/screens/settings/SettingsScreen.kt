package com.driftly.sleepsounds.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.PremiumGold
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceVariant
import com.driftly.sleepsounds.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPremium: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val resumeOnBluetooth by viewModel.resumeOnBluetooth.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val bedtimeReminderEnabled by viewModel.bedtimeReminderEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Premium upsell card
        if (!isPremium) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToPremium() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = PremiumGold
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Go Premium",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unlock all sounds, no ads, unlimited mixes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Playback settings
        SettingsSection(title = "Playback") {
            SettingsToggle(
                title = "Keep screen on during playback",
                checked = keepScreenOn,
                onCheckedChange = { viewModel.setKeepScreenOn(it) }
            )
            HorizontalDivider(color = Surface)
            SettingsToggle(
                title = "Resume on Bluetooth connect",
                checked = resumeOnBluetooth,
                onCheckedChange = { viewModel.setResumeOnBluetooth(it) }
            )
            HorizontalDivider(color = Surface)
            SettingsToggle(
                title = "Bedtime reminder",
                checked = bedtimeReminderEnabled,
                onCheckedChange = { viewModel.setBedtimeReminder(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio quality
        SettingsSection(title = "Audio") {
            val qualities = listOf("low", "medium", "high")
            qualities.forEach { quality ->
                SettingsRadioItem(
                    title = quality.replaceFirstChar { it.uppercase() },
                    selected = audioQuality == quality,
                    onClick = { viewModel.setAudioQuality(quality) }
                )
                if (quality != qualities.last()) {
                    HorizontalDivider(color = Surface)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About
        SettingsSection(title = "About") {
            SettingsClickable(title = "Restore Purchases", onClick = { /* TODO */ })
            HorizontalDivider(color = Surface)
            SettingsClickable(title = "Rate Us", onClick = { /* TODO */ })
            HorizontalDivider(color = Surface)
            SettingsClickable(title = "Privacy Policy", onClick = { /* TODO */ })
            HorizontalDivider(color = Surface)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}

@Composable
private fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) Primary else MaterialTheme.colorScheme.onBackground,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Text(
                text = "✓",
                color = Primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsClickable(
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
