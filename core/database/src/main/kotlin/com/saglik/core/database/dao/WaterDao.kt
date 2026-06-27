package com.saglik.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saglik.core.database.entity.WaterEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntryEntity)

    @Query(
        """
        SELECT * FROM water_entries
        WHERE deletedAt IS NULL
        ORDER BY recordedAt DESC
        """
    )
    fun observeWaterEntries(): Flow<List<WaterEntryEntity>>

    @Query(
        """
        SELECT * FROM water_entries
        WHERE deletedAt IS NULL
            AND recordedAt >= :startInclusive
            AND recordedAt < :endExclusive
        ORDER BY recordedAt ASC
        """
    )
    fun observeWaterEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<WaterEntryEntity>>

    @Query(
        """
        SELECT * FROM water_entries
        WHERE deletedAt IS NULL
        ORDER BY recordedAt DESC
        LIMIT 1
        """
    )
    fun observeLatestWaterEntry(): Flow<WaterEntryEntity?>

    @Query(
        """
        UPDATE water_entries
        SET deletedAt = :deletedAt, updatedAt = :deletedAt
        WHERE id = :id
        """
    )
    suspend fun softDeleteWaterEntry(
        id: String,
        deletedAt: Long,
    )
}
