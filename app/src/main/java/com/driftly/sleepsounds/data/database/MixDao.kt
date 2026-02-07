package com.driftly.sleepsounds.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MixDao {
    @Query("SELECT * FROM mixes ORDER BY isPreset DESC, createdAt DESC")
    fun getAllMixes(): Flow<List<MixEntity>>

    @Query("SELECT * FROM mixes WHERE isPreset = 0 ORDER BY createdAt DESC")
    fun getUserMixes(): Flow<List<MixEntity>>

    @Query("SELECT * FROM mixes WHERE isPreset = 1 ORDER BY createdAt ASC")
    fun getPresetMixes(): Flow<List<MixEntity>>

    @Query("SELECT COUNT(*) FROM mixes WHERE isPreset = 0")
    suspend fun getUserMixCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMix(mix: MixEntity): Long

    @Update
    suspend fun updateMix(mix: MixEntity)

    @Delete
    suspend fun deleteMix(mix: MixEntity)

    @Query("DELETE FROM mixes WHERE id = :id")
    suspend fun deleteMixById(id: Long)
}
