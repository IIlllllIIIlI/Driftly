package com.driftly.sleepsounds.presentation.screens.savedmixes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.driftly.sleepsounds.domain.models.Mix
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceVariant
import com.driftly.sleepsounds.presentation.viewmodels.HomeViewModel
import com.driftly.sleepsounds.presentation.viewmodels.SavedMixesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMixesScreen(
    onNavigateToPremium: () -> Unit,
    viewModel: SavedMixesViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val presetMixes by viewModel.presetMixes.collectAsState()
    val userMixes by viewModel.userMixes.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val showMaxMixesMessage by viewModel.showMaxMixesMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Mix?>(null) }
    var showRenameDialog by remember { mutableStateOf<Mix?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Saved Mixes",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (presetMixes.isNotEmpty()) {
                item {
                    Text(
                        text = "Soundscapes",
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(presetMixes, key = { it.id }) { mix ->
                    MixCard(
                        mix = mix,
                        onPlay = { homeViewModel.loadMix(mix.soundVolumes) },
                        onLongPress = { }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "My Mixes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (userMixes.isEmpty()) {
                item {
                    Text(
                        text = "No saved mixes yet.\nCreate one from the mixer!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(userMixes, key = { it.id }) { mix ->
                    MixCard(
                        mix = mix,
                        onPlay = { homeViewModel.loadMix(mix.soundVolumes) },
                        onLongPress = { showDeleteDialog = mix },
                        showDelete = true,
                        onDelete = { showDeleteDialog = mix }
                    )
                }
            }

            if (!isPremium && userMixes.size >= SavedMixesViewModel.MAX_FREE_MIXES) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Upgrade for unlimited mixes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(onClick = onNavigateToPremium) {
                                Text("Go Premium", color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { mix ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Mix") },
            text = { Text("Delete \"${mix.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMix(mix.id)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Surface
        )
    }

    // Max mixes message
    if (showMaxMixesMessage) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMaxMixesMessage() },
            title = { Text("Limit Reached") },
            text = { Text("Free users can save up to 3 mixes. Upgrade to Premium for unlimited mixes!") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissMaxMixesMessage()
                    onNavigateToPremium()
                }) {
                    Text("Go Premium", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissMaxMixesMessage() }) {
                    Text("OK")
                }
            },
            containerColor = Surface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MixCard(
    mix: Mix,
    onPlay: () -> Unit,
    onLongPress: () -> Unit,
    showDelete: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = mix.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${mix.soundVolumes.size} sounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (showDelete && onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(onClick = onPlay) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Primary
                )
            }
        }
    }
}
