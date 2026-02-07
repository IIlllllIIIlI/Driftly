package com.driftly.sleepsounds.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RawRes
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.driftly.sleepsounds.R
import com.driftly.sleepsounds.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioMixerService : Service() {

    private val binder = AudioMixerBinder()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val players = mutableMapOf<String, ExoPlayer>()
    private var wakeLock: PowerManager.WakeLock? = null
    private var timerJob: Job? = null

    private val _activeSounds = MutableStateFlow<Map<String, Float>>(emptyMap())
    val activeSounds: StateFlow<Map<String, Float>> = _activeSounds.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timerRemainingMs = MutableStateFlow(0L)
    val timerRemainingMs: StateFlow<Long> = _timerRemainingMs.asStateFlow()

    private val _timerTotalMs = MutableStateFlow(0L)
    val timerTotalMs: StateFlow<Long> = _timerTotalMs.asStateFlow()

    private var fadeOutDurationMs: Long = 30_000L
    private var originalVolumes: Map<String, Float> = emptyMap()

    inner class AudioMixerBinder : Binder() {
        fun getService(): AudioMixerService = this@AudioMixerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (_isPlaying.value) pauseAll() else playAll()
            }
            ACTION_STOP -> {
                stopAll()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        timerJob?.cancel()
        players.values.forEach { it.release() }
        players.clear()
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }

    fun addSound(soundId: String, @RawRes audioRes: Int, volume: Float = 0.7f) {
        if (players.size >= MAX_SIMULTANEOUS_SOUNDS && !players.containsKey(soundId)) return
        if (players.containsKey(soundId)) return

        val player = ExoPlayer.Builder(this).build().apply {
            val uri = "android.resource://${packageName}/$audioRes"
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            this.volume = volume
            prepare()
            playWhenReady = _isPlaying.value || players.isEmpty()
        }

        players[soundId] = player
        val updated = _activeSounds.value.toMutableMap()
        updated[soundId] = volume
        _activeSounds.value = updated

        if (players.isNotEmpty()) {
            _isPlaying.value = true
            acquireWakeLock()
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    fun removeSound(soundId: String) {
        players[soundId]?.release()
        players.remove(soundId)
        val updated = _activeSounds.value.toMutableMap()
        updated.remove(soundId)
        _activeSounds.value = updated

        if (players.isEmpty()) {
            _isPlaying.value = false
            releaseWakeLock()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            updateNotification()
        }
    }

    fun setVolume(soundId: String, volume: Float) {
        players[soundId]?.volume = volume.coerceIn(0f, 1f)
        val updated = _activeSounds.value.toMutableMap()
        updated[soundId] = volume.coerceIn(0f, 1f)
        _activeSounds.value = updated
    }

    fun playAll() {
        players.values.forEach { it.playWhenReady = true }
        _isPlaying.value = true
        if (players.isNotEmpty()) {
            acquireWakeLock()
            updateNotification()
        }
    }

    fun pauseAll() {
        players.values.forEach { it.playWhenReady = false }
        _isPlaying.value = false
        releaseWakeLock()
        updateNotification()
    }

    fun stopAll() {
        timerJob?.cancel()
        _timerRemainingMs.value = 0
        _timerTotalMs.value = 0
        players.values.forEach { it.release() }
        players.clear()
        _activeSounds.value = emptyMap()
        _isPlaying.value = false
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    fun startTimer(durationMs: Long, fadeOutMs: Long) {
        timerJob?.cancel()
        _timerTotalMs.value = durationMs
        _timerRemainingMs.value = durationMs
        fadeOutDurationMs = fadeOutMs
        originalVolumes = _activeSounds.value.toMap()

        timerJob = scope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMs

            while (System.currentTimeMillis() < endTime) {
                val remaining = endTime - System.currentTimeMillis()
                _timerRemainingMs.value = remaining

                if (remaining <= fadeOutMs) {
                    val fadeProgress = remaining.toFloat() / fadeOutMs.toFloat()
                    originalVolumes.forEach { (id, vol) ->
                        setVolume(id, vol * fadeProgress)
                    }
                }

                delay(1000)
            }

            _timerRemainingMs.value = 0
            _timerTotalMs.value = 0
            stopAll()
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        _timerRemainingMs.value = 0
        _timerTotalMs.value = 0
        // Restore original volumes
        originalVolumes.forEach { (id, vol) ->
            setVolume(id, vol)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "Driftly::AudioPlayback"
            ).apply {
                acquire(8 * 60 * 60 * 1000L) // 8 hours max
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIntent = PendingIntent.getService(
            this, 1,
            Intent(this, AudioMixerService::class.java).setAction(ACTION_PLAY_PAUSE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this, 2,
            Intent(this, AudioMixerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIcon = if (_isPlaying.value) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (_isPlaying.value) "Pause" else "Play"

        val contentText = if (_isPlaying.value) {
            getString(R.string.notification_playing, players.size)
        } else {
            getString(R.string.notification_paused)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(contentIntent)
            .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        if (players.isNotEmpty()) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    companion object {
        const val CHANNEL_ID = "driftly_playback"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "com.driftly.sleepsounds.PLAY_PAUSE"
        const val ACTION_STOP = "com.driftly.sleepsounds.STOP"
        const val MAX_SIMULTANEOUS_SOUNDS = 5
    }
}
