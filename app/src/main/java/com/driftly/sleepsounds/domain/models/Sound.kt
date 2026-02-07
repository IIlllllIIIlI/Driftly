package com.driftly.sleepsounds.domain.models

import androidx.annotation.RawRes

data class Sound(
    val id: String,
    val name: String,
    val category: SoundCategory,
    val icon: String,
    @RawRes val audioRes: Int,
    val isPremium: Boolean = false
)

enum class SoundCategory(val displayName: String) {
    NATURE("Nature"),
    WATER("Water"),
    URBAN("Urban"),
    NOISE("Noise"),
    COZY("Cozy")
}
