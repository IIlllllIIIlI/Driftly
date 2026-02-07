package com.driftly.sleepsounds.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.driftly.sleepsounds.domain.models.SoundCategory
import com.driftly.sleepsounds.presentation.components.MixerBar
import com.driftly.sleepsounds.presentation.components.SoundCard
import com.driftly.sleepsounds.presentation.components.TimerSheet
import com.driftly.sleepsounds.presentation.theme.OnSurfaceDim
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.PrimaryDim
import com.driftly.sleepsounds.presentation.theme.Secondary
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceVariant
import com.driftly.sleepsounds.presentation.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPremium: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val filteredSounds by viewModel.filteredSounds.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val favoriteSoundIds by viewModel.favoriteSoundIds.collectAsState()

    val service by viewModel.service.collectAsState()
    val activeSounds by (service?.activeSounds ?: remember {
        kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, Float>())
    }).collectAsState()
    val isPlaying by (service?.isPlaying ?: remember {
        kotlinx.coroutines.flow.MutableStateFlow(false)
    }).collectAsState()
    val timerRemainingMs by (service?.timerRemainingMs ?: remember {
        kotlinx.coroutines.flow.MutableStateFlow(0L)
    }).collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showTimerSheet by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf<String?>(null) }

    val categories = listOf(null) + SoundCategory.entries
    val categoryLabels = listOf("All") + SoundCategory.entries.map { it.displayName }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header Section ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Moon icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83C\uDF19", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Driftly",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Sleep Sounds & White Noise",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDim
                    )
                }

                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(
                        if (showSearch) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = { viewModel.toggleFavoritesOnly() }) {
                    Icon(
                        imageVector = if (showFavoritesOnly) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorites",
                        tint = if (showFavoritesOnly) Primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── Search bar (animated) ──
        AnimatedVisibility(
            visible = showSearch,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Search sounds...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDim
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Primary
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }

        // ── Category chips ──
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = selectedCategory == categories[index],
                    onClick = { viewModel.selectCategory(categories[index]) },
                    label = {
                        Text(
                            categoryLabels[index],
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Surface,
                        containerColor = SurfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = selectedCategory == categories[index]
                    )
                )
            }
        }

        // ── Sound grid (3 columns) ──
        Box(modifier = Modifier.weight(1f)) {
            if (filteredSounds.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "\uD83D\uDD0A", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No sounds found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try a different category or search term",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSounds, key = { it.id }) { sound ->
                        SoundCard(
                            sound = sound,
                            isActive = sound.id in activeSounds,
                            isPremiumUser = isPremium,
                            onClick = {
                                if (sound.isPremium && !isPremium) {
                                    showAdDialog = sound.id
                                } else {
                                    viewModel.toggleSound(sound)
                                }
                            },
                            modifier = Modifier.aspectRatio(0.9f)
                        )
                    }
                }
            }
        }

        // ── Mixer bar ──
        MixerBar(
            activeSounds = activeSounds,
            allSounds = viewModel.allSounds,
            isPlaying = isPlaying,
            timerRemainingMs = timerRemainingMs,
            onPlayPause = { viewModel.playPause() },
            onStop = { viewModel.stopAll() },
            onVolumeChange = { id, vol -> viewModel.setVolume(id, vol) },
            onRemoveSound = { viewModel.removeSound(it) },
            onTimerClick = { showTimerSheet = true },
            onSaveMix = { /* handled by SavedMixesViewModel */ }
        )
    }

    // ── Timer bottom sheet ──
    if (showTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceVariant,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            TimerSheet(
                isTimerActive = timerRemainingMs > 0,
                onStartTimer = { duration, fade -> viewModel.startTimer(duration, fade) },
                onCancelTimer = { viewModel.cancelTimer() },
                onDismiss = { showTimerSheet = false }
            )
        }
    }

    // ── Ad dialog for premium sounds ──
    if (showAdDialog != null) {
        val soundId = showAdDialog!!
        val sound = viewModel.allSounds.find { it.id == soundId }
        AlertDialog(
            onDismissRequest = { showAdDialog = null },
            title = {
                Text(
                    "Unlock ${sound?.name ?: "Sound"}",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Watch a short video to unlock this sound for 24 hours, or upgrade to Premium for full access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // TODO: Show rewarded ad, then unlock
                    showAdDialog = null
                }) {
                    Text("Watch Video", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onNavigateToPremium()
                    showAdDialog = null
                }) {
                    Text("Go Premium", color = Secondary)
                }
            },
            containerColor = SurfaceVariant,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
