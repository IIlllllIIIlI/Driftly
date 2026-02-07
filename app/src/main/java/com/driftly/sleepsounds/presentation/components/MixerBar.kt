package com.driftly.sleepsounds.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driftly.sleepsounds.domain.models.Sound
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.SurfaceVariant

@Composable
fun MixerBar(
    activeSounds: Map<String, Float>,
    allSounds: List<Sound>,
    isPlaying: Boolean,
    timerRemainingMs: Long,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    onRemoveSound: (String) -> Unit,
    onTimerClick: () -> Unit,
    onSaveMix: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = activeSounds.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Collapsed bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    IconButton(onClick = onPlayPause, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${activeSounds.size} sound${if (activeSounds.size != 1) "s" else ""} playing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (timerRemainingMs > 0) {
                            Text(
                                text = formatTimer(timerRemainingMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }

                    IconButton(onClick = onTimerClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (timerRemainingMs > 0) Primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop all",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Expanded content
                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))

                    activeSounds.forEach { (soundId, volume) ->
                        val sound = allSounds.find { it.id == soundId }
                        if (sound != null) {
                            SoundVolumeRow(
                                sound = sound,
                                volume = volume,
                                onVolumeChange = { onVolumeChange(soundId, it) },
                                onRemove = { onRemoveSound(soundId) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Save Mix",
                            color = Primary,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .clickable { onSaveMix() }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundVolumeRow(
    sound: Sound,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = sound.icon,
            fontSize = 18.sp,
            modifier = Modifier.width(28.dp)
        )

        Text(
            text = sound.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(70.dp)
        )

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = SurfaceVariant
            )
        )

        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp)
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatTimer(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d remaining", hours, minutes, seconds)
    } else {
        String.format("%d:%02d remaining", minutes, seconds)
    }
}
