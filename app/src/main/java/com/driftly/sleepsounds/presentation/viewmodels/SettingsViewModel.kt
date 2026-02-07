package com.driftly.sleepsounds.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driftly.sleepsounds.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = userPreferences.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val keepScreenOn: StateFlow<Boolean> = userPreferences.keepScreenOn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val resumeOnBluetooth: StateFlow<Boolean> = userPreferences.resumeOnBluetooth
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val audioQuality: StateFlow<String> = userPreferences.audioQuality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "high")

    val bedtimeReminderEnabled: StateFlow<Boolean> = userPreferences.bedtimeReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bedtimeReminderTime: StateFlow<String> = userPreferences.bedtimeReminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "22:00")

    fun setKeepScreenOn(value: Boolean) {
        viewModelScope.launch { userPreferences.setKeepScreenOn(value) }
    }

    fun setResumeOnBluetooth(value: Boolean) {
        viewModelScope.launch { userPreferences.setResumeOnBluetooth(value) }
    }

    fun setAudioQuality(value: String) {
        viewModelScope.launch { userPreferences.setAudioQuality(value) }
    }

    fun setBedtimeReminder(enabled: Boolean, time: String? = null) {
        viewModelScope.launch { userPreferences.setBedtimeReminder(enabled, time) }
    }
}
