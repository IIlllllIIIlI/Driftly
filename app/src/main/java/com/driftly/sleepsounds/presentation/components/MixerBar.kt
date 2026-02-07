package com.driftly.sleepsounds.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driftly.sleepsounds.domain.models.Sound
import com.driftly.sleepsounds.presentation.theme.Background
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.PrimaryDim
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceElevated
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
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Surface)
                        .clickable { expanded = !expanded }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Collapsed header row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    // Play/Pause button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onPlayPause, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Background,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Now Playing",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${activeSounds.size} sound${if (activeSounds.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (timerRemainingMs > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryDim.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formatTimer(timerRemainingMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(onClick = onTimerClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (timerRemainingMs > 0) Primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onStop, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop all",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Expanded content
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Surface, thickness = 0.5.dp)
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
                        HorizontalDivider(color = Surface, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Bottom actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onSaveMix) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Save Mix",
                                    color = Primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
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
            .padding(vertical = 4.dp)
    ) {
        // Sound icon in small circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sound.icon,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = sound.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(72.dp)
        )

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier
                .weight(1f)
                .height(32.dp),
            colors = SliderDefaults.colors(
                thumbColor = Primary,
                activeTrackColor = Primary,
                inactiveTrackColor = SurfaceElevated
            )
        )

        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp)
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
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
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
