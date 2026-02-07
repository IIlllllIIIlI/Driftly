package com.driftly.sleepsounds.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mixes")
data class MixEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val soundVolumesJson: String,
    val isPreset: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
