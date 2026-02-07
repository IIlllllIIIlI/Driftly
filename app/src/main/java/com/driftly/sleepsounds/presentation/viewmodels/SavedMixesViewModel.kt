package com.driftly.sleepsounds.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driftly.sleepsounds.data.preferences.UserPreferences
import com.driftly.sleepsounds.data.repository.MixRepository
import com.driftly.sleepsounds.domain.models.Mix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedMixesViewModel @Inject constructor(
    private val mixRepository: MixRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val userMixes: StateFlow<List<Mix>> = mixRepository.getUserMixes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presetMixes: StateFlow<List<Mix>> = mixRepository.getPresetMixes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPremium: StateFlow<Boolean> = userPreferences.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _showMaxMixesMessage = MutableStateFlow(false)
    val showMaxMixesMessage: StateFlow<Boolean> = _showMaxMixesMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Insert presets if needed
            if (presetMixes.value.isEmpty()) {
                mixRepository.insertPresets()
            }
        }
    }

    fun saveMix(name: String, soundVolumes: Map<String, Float>) {
        viewModelScope.launch {
            val count = mixRepository.getUserMixCount()
            if (!isPremium.value && count >= MAX_FREE_MIXES) {
                _showMaxMixesMessage.value = true
                return@launch
            }
            mixRepository.saveMix(Mix(name = name, soundVolumes = soundVolumes))
            _showSaveDialog.value = false
        }
    }

    fun deleteMix(id: Long) {
        viewModelScope.launch {
            mixRepository.deleteMix(id)
        }
    }

    fun renameMix(mix: Mix, newName: String) {
        viewModelScope.launch {
            mixRepository.updateMix(mix.copy(name = newName))
        }
    }

    fun showSaveDialog() {
        _showSaveDialog.value = true
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
    }

    fun dismissMaxMixesMessage() {
        _showMaxMixesMessage.value = false
    }

    companion object {
        const val MAX_FREE_MIXES = 3
    }
}
