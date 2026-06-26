package com.saglik.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saglik.core.database.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_entries ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatestWeightEntry(): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM weight_entries ORDER BY recordedAt DESC")
    fun observeWeightEntries(): Flow<List<WeightEntryEntity>>
}
