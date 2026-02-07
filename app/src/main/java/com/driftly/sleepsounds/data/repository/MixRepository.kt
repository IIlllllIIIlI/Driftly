package com.driftly.sleepsounds.data.repository

import com.driftly.sleepsounds.data.database.MixDao
import com.driftly.sleepsounds.data.database.MixEntity
import com.driftly.sleepsounds.domain.models.Mix
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MixRepository @Inject constructor(
    private val mixDao: MixDao
) {
    fun getAllMixes(): Flow<List<Mix>> = mixDao.getAllMixes().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getUserMixes(): Flow<List<Mix>> = mixDao.getUserMixes().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getPresetMixes(): Flow<List<Mix>> = mixDao.getPresetMixes().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getUserMixCount(): Int = mixDao.getUserMixCount()

    suspend fun saveMix(mix: Mix): Long {
        return mixDao.insertMix(mix.toEntity())
    }

    suspend fun updateMix(mix: Mix) {
        mixDao.updateMix(mix.toEntity())
    }

    suspend fun deleteMix(id: Long) {
        mixDao.deleteMixById(id)
    }

    suspend fun insertPresets() {
        val existingPresets = mixDao.getPresetMixes()
        // Only insert if we haven't already
        presetMixes.forEach { mix ->
            mixDao.insertMix(mix.toEntity())
        }
    }

    companion object {
        val presetMixes = listOf(
            Mix(
                name = "Rainy Night",
                soundVolumes = mapOf("rain" to 0.7f, "thunder" to 0.3f, "wind" to 0.2f),
                isPreset = true
            ),
            Mix(
                name = "Beach Sunset",
                soundVolumes = mapOf("ocean_waves" to 0.8f, "seagulls" to 0.3f, "light_breeze" to 0.4f),
                isPreset = true
            ),
            Mix(
                name = "Cozy Cabin",
                soundVolumes = mapOf("fireplace" to 0.6f, "rain_on_window" to 0.5f, "clock" to 0.2f),
                isPreset = true
            ),
            Mix(
                name = "Deep Focus",
                soundVolumes = mapOf("brown_noise" to 0.6f, "cafe" to 0.3f),
                isPreset = true
            ),
            Mix(
                name = "Forest Morning",
                soundVolumes = mapOf("birds" to 0.5f, "river" to 0.4f, "rustling_leaves" to 0.3f),
                isPreset = true
            ),
        )
    }
}

private fun MixEntity.toDomain(): Mix = Mix(
    id = id,
    name = name,
    soundVolumes = parseSoundVolumes(soundVolumesJson),
    isPreset = isPreset
)

private fun Mix.toEntity(): MixEntity = MixEntity(
    id = id,
    name = name,
    soundVolumesJson = serializeSoundVolumes(soundVolumes),
    isPreset = isPreset
)

private fun parseSoundVolumes(json: String): Map<String, Float> {
    return try {
        val obj = JSONObject(json)
        val map = mutableMapOf<String, Float>()
        obj.keys().forEach { key ->
            map[key] = obj.getDouble(key).toFloat()
        }
        map
    } catch (e: Exception) {
        emptyMap()
    }
}

private fun serializeSoundVolumes(volumes: Map<String, Float>): String {
    val obj = JSONObject()
    volumes.forEach { (key, value) -> obj.put(key, value.toDouble()) }
    return obj.toString()
}
