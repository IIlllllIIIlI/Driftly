package com.driftly.sleepsounds.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioServiceConnection(private val context: Context) {

    private var _service = MutableStateFlow<AudioMixerService?>(null)
    val service: StateFlow<AudioMixerService?> = _service.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val mixerBinder = binder as AudioMixerService.AudioMixerBinder
            _service.value = mixerBinder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _service.value = null
        }
    }

    fun bind() {
        val intent = Intent(context, AudioMixerService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        try {
            context.unbindService(connection)
        } catch (_: IllegalArgumentException) {
            // Not bound
        }
        _service.value = null
    }
}
