package com.driftly.sleepsounds.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.driftly.sleepsounds.data.preferences.UserPreferences
import com.driftly.sleepsounds.data.repository.SoundRepository
import com.driftly.sleepsounds.domain.models.Sound
import com.driftly.sleepsounds.domain.models.SoundCategory
import com.driftly.sleepsounds.service.AudioMixerService
import com.driftly.sleepsounds.service.AudioServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val soundRepository: SoundRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    private val serviceConnection = AudioServiceConnection(application)

    private val _selectedCategory = MutableStateFlow<SoundCategory?>(null)
    val selectedCategory: StateFlow<SoundCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _showMixerSheet = MutableStateFlow(false)
    val showMixerSheet: StateFlow<Boolean> = _showMixerSheet.asStateFlow()

    val isPremium: StateFlow<Boolean> = userPreferences.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val favoriteSoundIds: StateFlow<Set<String>> = userPreferences.favoriteSoundIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allSounds: List<Sound> = soundRepository.getAllSounds()

    val filteredSounds: StateFlow<List<Sound>> = combine(
        _selectedCategory,
        _searchQuery,
        _showFavoritesOnly,
        userPreferences.favoriteSoundIds
    ) { category, query, favOnly, favorites ->
        var sounds = allSounds
        if (category != null) {
            sounds = sounds.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            sounds = sounds.filter { it.name.contains(query, ignoreCase = true) }
        }
        if (favOnly) {
            sounds = sounds.filter { it.id in favorites }
        }
        sounds
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allSounds)

    // Service state
    val service: StateFlow<AudioMixerService?> = serviceConnection.service

    val activeSounds: StateFlow<Map<String, Float>>
        get() = serviceConnection.service.value?.activeSounds
            ?: MutableStateFlow(emptyMap())

    val isPlaying: StateFlow<Boolean>
        get() = serviceConnection.service.value?.isPlaying
            ?: MutableStateFlow(false)

    val timerRemainingMs: StateFlow<Long>
        get() = serviceConnection.service.value?.timerRemainingMs
            ?: MutableStateFlow(0L)

    init {
        serviceConnection.bind()
    }

    override fun onCleared() {
        serviceConnection.unbind()
        super.onCleared()
    }

    fun selectCategory(category: SoundCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleFavorite(soundId: String) {
        viewModelScope.launch {
            userPreferences.toggleFavorite(soundId)
        }
    }

    fun toggleSound(sound: Sound) {
        val svc = serviceConnection.service.value ?: return
        val active = svc.activeSounds.value
        if (sound.id in active) {
            svc.removeSound(sound.id)
        } else {
            svc.addSound(sound.id, sound.audioRes)
        }
    }

    fun setVolume(soundId: String, volume: Float) {
        serviceConnection.service.value?.setVolume(soundId, volume)
    }

    fun removeSound(soundId: String) {
        serviceConnection.service.value?.removeSound(soundId)
    }

    fun playPause() {
        val svc = serviceConnection.service.value ?: return
        if (svc.isPlaying.value) svc.pauseAll() else svc.playAll()
    }

    fun stopAll() {
        serviceConnection.service.value?.stopAll()
    }

    fun toggleMixerSheet() {
        _showMixerSheet.value = !_showMixerSheet.value
    }

    fun setMixerSheetVisible(visible: Boolean) {
        _showMixerSheet.value = visible
    }

    fun startTimer(durationMs: Long, fadeOutMs: Long) {
        serviceConnection.service.value?.startTimer(durationMs, fadeOutMs)
    }

    fun cancelTimer() {
        serviceConnection.service.value?.cancelTimer()
    }

    fun loadMix(soundVolumes: Map<String, Float>) {
        val svc = serviceConnection.service.value ?: return
        svc.stopAll()
        soundVolumes.forEach { (soundId, volume) ->
            val sound = soundRepository.getSoundById(soundId) ?: return@forEach
            svc.addSound(soundId, sound.audioRes, volume)
        }
    }
}
