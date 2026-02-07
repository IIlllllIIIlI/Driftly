package com.driftly.sleepsounds.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.driftly.sleepsounds.domain.models.SoundCategory
import com.driftly.sleepsounds.presentation.components.MixerBar
import com.driftly.sleepsounds.presentation.components.SoundCard
import com.driftly.sleepsounds.presentation.components.TimerSheet
import com.driftly.sleepsounds.presentation.theme.Primary
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
        // Top bar
        TopAppBar(
            title = {
                Text(
                    text = "Driftly",
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { viewModel.toggleFavoritesOnly() }) {
                    Icon(
                        imageVector = if (showFavoritesOnly) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorites",
                        tint = if (showFavoritesOnly) Primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Search bar
        AnimatedVisibility(visible = showSearch) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search sounds...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = selectedCategory == categories[index],
                    onClick = { viewModel.selectCategory(categories[index]) },
                    label = { Text(categoryLabels[index]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Surface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sound grid
        Box(modifier = Modifier.weight(1f)) {
            if (filteredSounds.isEmpty()) {
                Text(
                    text = "No sounds found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            }
                        )
                    }
                }
            }
        }

        // Mixer bar
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

    // Timer bottom sheet
    if (showTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Surface
        ) {
            TimerSheet(
                isTimerActive = timerRemainingMs > 0,
                onStartTimer = { duration, fade -> viewModel.startTimer(duration, fade) },
                onCancelTimer = { viewModel.cancelTimer() },
                onDismiss = { showTimerSheet = false }
            )
        }
    }

    // Ad dialog for premium sounds
    if (showAdDialog != null) {
        val soundId = showAdDialog!!
        val sound = viewModel.allSounds.find { it.id == soundId }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAdDialog = null },
            title = { Text("Unlock ${sound?.name ?: "Sound"}") },
            text = { Text("Watch a short video to unlock this sound for 24 hours, or upgrade to Premium for full access.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    // TODO: Show rewarded ad, then unlock
                    showAdDialog = null
                }) {
                    Text("Watch Video", color = Primary)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onNavigateToPremium()
                    showAdDialog = null
                }) {
                    Text("Go Premium", color = Primary)
                }
            },
            containerColor = Surface
        )
    }
}
