package com.driftly.sleepsounds.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "driftly_prefs")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val RESUME_ON_BLUETOOTH = booleanPreferencesKey("resume_on_bluetooth")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
        val BEDTIME_REMINDER_ENABLED = booleanPreferencesKey("bedtime_reminder_enabled")
        val BEDTIME_REMINDER_TIME = stringPreferencesKey("bedtime_reminder_time")
        val FAVORITE_SOUND_IDS = stringSetPreferencesKey("favorite_sound_ids")
        val INTERSTITIAL_SHOWN_THIS_SESSION = booleanPreferencesKey("interstitial_shown")
        val UNLOCKED_PREMIUM_SOUNDS = stringSetPreferencesKey("unlocked_premium_sounds")
        val PREMIUM_SOUND_UNLOCK_TIMES = stringPreferencesKey("premium_sound_unlock_times")
        val FIRST_OPEN_TIMESTAMP = longPreferencesKey("first_open_timestamp")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_PREMIUM] ?: false }
    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { it[Keys.KEEP_SCREEN_ON] ?: false }
    val resumeOnBluetooth: Flow<Boolean> = context.dataStore.data.map { it[Keys.RESUME_ON_BLUETOOTH] ?: false }
    val audioQuality: Flow<String> = context.dataStore.data.map { it[Keys.AUDIO_QUALITY] ?: "high" }
    val bedtimeReminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BEDTIME_REMINDER_ENABLED] ?: false }
    val bedtimeReminderTime: Flow<String> = context.dataStore.data.map { it[Keys.BEDTIME_REMINDER_TIME] ?: "22:00" }
    val favoriteSoundIds: Flow<Set<String>> = context.dataStore.data.map { it[Keys.FAVORITE_SOUND_IDS] ?: emptySet() }

    suspend fun setPremium(value: Boolean) {
        context.dataStore.edit { it[Keys.IS_PREMIUM] = value }
    }

    suspend fun setKeepScreenOn(value: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = value }
    }

    suspend fun setResumeOnBluetooth(value: Boolean) {
        context.dataStore.edit { it[Keys.RESUME_ON_BLUETOOTH] = value }
    }

    suspend fun setAudioQuality(value: String) {
        context.dataStore.edit { it[Keys.AUDIO_QUALITY] = value }
    }

    suspend fun setBedtimeReminder(enabled: Boolean, time: String? = null) {
        context.dataStore.edit {
            it[Keys.BEDTIME_REMINDER_ENABLED] = enabled
            if (time != null) it[Keys.BEDTIME_REMINDER_TIME] = time
        }
    }

    suspend fun toggleFavorite(soundId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_SOUND_IDS] ?: emptySet()
            prefs[Keys.FAVORITE_SOUND_IDS] = if (soundId in current) {
                current - soundId
            } else {
                current + soundId
            }
        }
    }

    suspend fun setInterstitialShown(shown: Boolean) {
        context.dataStore.edit { it[Keys.INTERSTITIAL_SHOWN_THIS_SESSION] = shown }
    }

    val interstitialShownThisSession: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.INTERSTITIAL_SHOWN_THIS_SESSION] ?: false }

    suspend fun unlockPremiumSound(soundId: String) {
        context.dataStore.edit { prefs ->
            val unlocked = prefs[Keys.UNLOCKED_PREMIUM_SOUNDS]?.toMutableSet() ?: mutableSetOf()
            unlocked.add(soundId)
            prefs[Keys.UNLOCKED_PREMIUM_SOUNDS] = unlocked

            val times = prefs[Keys.PREMIUM_SOUND_UNLOCK_TIMES] ?: ""
            val map = parseTimes(times).toMutableMap()
            map[soundId] = System.currentTimeMillis()
            prefs[Keys.PREMIUM_SOUND_UNLOCK_TIMES] = serializeTimes(map)
        }
    }

    fun isSoundTemporarilyUnlocked(soundId: String): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            val times = prefs[Keys.PREMIUM_SOUND_UNLOCK_TIMES] ?: ""
            val map = parseTimes(times)
            val unlockTime = map[soundId] ?: return@map false
            System.currentTimeMillis() - unlockTime < 24 * 60 * 60 * 1000
        }
    }

    private fun parseTimes(serialized: String): Map<String, Long> {
        if (serialized.isBlank()) return emptyMap()
        return serialized.split(";").mapNotNull { entry ->
            val parts = entry.split("=")
            if (parts.size == 2) parts[0] to parts[1].toLongOrNull()?.let { it } else null
        }.mapNotNull { (k, v) -> if (v != null) k to v else null }.toMap()
    }

    private fun serializeTimes(map: Map<String, Long>): String {
        return map.entries.joinToString(";") { "${it.key}=${it.value}" }
    }
}
