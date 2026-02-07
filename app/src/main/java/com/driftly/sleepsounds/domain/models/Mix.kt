package com.driftly.sleepsounds.domain.models

data class Mix(
    val id: Long = 0,
    val name: String,
    val soundVolumes: Map<String, Float>,
    val isPreset: Boolean = false
)
