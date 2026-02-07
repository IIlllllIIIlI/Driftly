package com.driftly.sleepsounds.domain.models

data class TimerState(
    val isActive: Boolean = false,
    val totalDurationMs: Long = 0,
    val remainingMs: Long = 0,
    val fadeOutDurationMs: Long = 30_000
)
