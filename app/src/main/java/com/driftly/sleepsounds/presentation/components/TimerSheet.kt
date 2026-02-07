package com.driftly.sleepsounds.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.Surface

data class TimerOption(val label: String, val durationMs: Long)
data class FadeOption(val label: String, val durationMs: Long)

private val timerOptions = listOf(
    TimerOption("15 min", 15 * 60 * 1000L),
    TimerOption("30 min", 30 * 60 * 1000L),
    TimerOption("45 min", 45 * 60 * 1000L),
    TimerOption("1 hr", 60 * 60 * 1000L),
    TimerOption("2 hr", 2 * 60 * 60 * 1000L),
    TimerOption("4 hr", 4 * 60 * 60 * 1000L),
    TimerOption("8 hr", 8 * 60 * 60 * 1000L),
)

private val fadeOptions = listOf(
    FadeOption("10 sec", 10_000L),
    FadeOption("30 sec", 30_000L),
    FadeOption("1 min", 60_000L),
    FadeOption("5 min", 5 * 60_000L),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerSheet(
    isTimerActive: Boolean,
    onStartTimer: (durationMs: Long, fadeOutMs: Long) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDuration by remember { mutableLongStateOf(30 * 60 * 1000L) }
    var selectedFade by remember { mutableLongStateOf(30_000L) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Sleep Timer",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Duration",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timerOptions.forEach { option ->
                FilterChip(
                    selected = selectedDuration == option.durationMs,
                    onClick = { selectedDuration = option.durationMs },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Surface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Fade out over",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fadeOptions.forEach { option ->
                FilterChip(
                    selected = selectedFade == option.durationMs,
                    onClick = { selectedFade = option.durationMs },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Surface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isTimerActive) {
                OutlinedButton(
                    onClick = {
                        onCancelTimer()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel Timer")
                }
            }

            Button(
                onClick = {
                    onStartTimer(selectedDuration, selectedFade)
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = if (isTimerActive) "Update Timer" else "Start Timer",
                    color = Surface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
