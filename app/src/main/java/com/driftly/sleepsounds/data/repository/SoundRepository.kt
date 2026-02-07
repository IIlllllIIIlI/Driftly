package com.driftly.sleepsounds.data.repository

import com.driftly.sleepsounds.R
import com.driftly.sleepsounds.domain.models.Sound
import com.driftly.sleepsounds.domain.models.SoundCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundRepository @Inject constructor() {

    fun getAllSounds(): List<Sound> = allSounds

    fun getSoundById(id: String): Sound? = allSounds.find { it.id == id }

    fun getSoundsByCategory(category: SoundCategory): List<Sound> =
        allSounds.filter { it.category == category }

    fun searchSounds(query: String): List<Sound> =
        allSounds.filter { it.name.contains(query, ignoreCase = true) }

    companion object {
        private val allSounds = listOf(
            // Nature
            Sound("rain", "Rain", SoundCategory.NATURE, "🌧️", R.raw.white_noise),
            Sound("thunder", "Thunder", SoundCategory.NATURE, "⛈️", R.raw.white_noise),
            Sound("forest", "Forest", SoundCategory.NATURE, "🌲", R.raw.white_noise),
            Sound("wind", "Wind", SoundCategory.NATURE, "💨", R.raw.white_noise),
            Sound("birds", "Birds", SoundCategory.NATURE, "🐦", R.raw.white_noise),
            Sound("crickets", "Crickets", SoundCategory.NATURE, "🦗", R.raw.white_noise),
            Sound("frogs", "Frogs", SoundCategory.NATURE, "🐸", R.raw.white_noise, isPremium = true),
            Sound("campfire", "Campfire", SoundCategory.NATURE, "🔥", R.raw.white_noise),

            // Water
            Sound("ocean_waves", "Ocean Waves", SoundCategory.WATER, "🌊", R.raw.pink_noise),
            Sound("river", "River", SoundCategory.WATER, "🏞️", R.raw.pink_noise),
            Sound("rain_on_window", "Rain on Window", SoundCategory.WATER, "🪟", R.raw.pink_noise),
            Sound("dripping", "Dripping", SoundCategory.WATER, "💧", R.raw.pink_noise, isPremium = true),
            Sound("waterfall", "Waterfall", SoundCategory.WATER, "⛰️", R.raw.pink_noise),
            Sound("gentle_stream", "Gentle Stream", SoundCategory.WATER, "🌿", R.raw.pink_noise, isPremium = true),
            Sound("heavy_rain", "Heavy Rain", SoundCategory.WATER, "🌧️", R.raw.pink_noise),
            Sound("shower", "Shower", SoundCategory.WATER, "🚿", R.raw.pink_noise, isPremium = true),

            // Urban
            Sound("cafe", "Café", SoundCategory.URBAN, "☕", R.raw.brown_noise),
            Sound("airplane", "Airplane", SoundCategory.URBAN, "✈️", R.raw.brown_noise),
            Sound("train", "Train", SoundCategory.URBAN, "🚂", R.raw.brown_noise),
            Sound("traffic", "Traffic", SoundCategory.URBAN, "🚗", R.raw.brown_noise, isPremium = true),
            Sound("keyboard", "Keyboard", SoundCategory.URBAN, "⌨️", R.raw.brown_noise),
            Sound("city_rain", "City Rain", SoundCategory.URBAN, "🌃", R.raw.brown_noise, isPremium = true),
            Sound("library", "Library", SoundCategory.URBAN, "📚", R.raw.brown_noise),

            // Noise
            Sound("white_noise", "White Noise", SoundCategory.NOISE, "📻", R.raw.white_noise),
            Sound("pink_noise", "Pink Noise", SoundCategory.NOISE, "🎀", R.raw.pink_noise),
            Sound("brown_noise", "Brown Noise", SoundCategory.NOISE, "🟤", R.raw.brown_noise),
            Sound("fan", "Fan", SoundCategory.NOISE, "🌀", R.raw.white_noise),
            Sound("ac", "Air Conditioner", SoundCategory.NOISE, "❄️", R.raw.white_noise, isPremium = true),
            Sound("vacuum", "Vacuum", SoundCategory.NOISE, "🧹", R.raw.white_noise, isPremium = true),
            Sound("dryer", "Dryer", SoundCategory.NOISE, "🫧", R.raw.white_noise),

            // Cozy
            Sound("fireplace", "Fireplace", SoundCategory.COZY, "🔥", R.raw.brown_noise),
            Sound("clock", "Clock Ticking", SoundCategory.COZY, "🕐", R.raw.brown_noise),
            Sound("washing_machine", "Washing Machine", SoundCategory.COZY, "🫧", R.raw.brown_noise, isPremium = true),
            Sound("cat_purring", "Cat Purring", SoundCategory.COZY, "🐱", R.raw.brown_noise),
            Sound("pages", "Pages Turning", SoundCategory.COZY, "📖", R.raw.brown_noise),
            Sound("wind_chimes", "Wind Chimes", SoundCategory.COZY, "🎐", R.raw.brown_noise, isPremium = true),
            Sound("night_ambience", "Night Ambience", SoundCategory.COZY, "🌙", R.raw.brown_noise),
            Sound("seagulls", "Seagulls", SoundCategory.COZY, "🕊️", R.raw.white_noise),
            Sound("rustling_leaves", "Rustling Leaves", SoundCategory.COZY, "🍃", R.raw.brown_noise),
            Sound("light_breeze", "Light Breeze", SoundCategory.COZY, "🌬️", R.raw.white_noise),
        )
    }
}
