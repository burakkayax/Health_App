package com.saglik.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saglik.core.database.entity.SleepEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_entries ORDER BY endTime DESC")
    fun observeAllSleepEntries(): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM sleep_entries ORDER BY endTime DESC LIMIT 1")
    fun observeLatestSleepEntry(): Flow<SleepEntryEntity?>

    @Query(
        """
        SELECT * FROM sleep_entries
        WHERE endTime >= :startInclusive AND endTime < :endExclusive
        ORDER BY startTime ASC
        """,
    )
    fun observeSleepEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<SleepEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepEntry(entry: SleepEntryEntity)

    @Delete
    suspend fun deleteSleepEntry(entry: SleepEntryEntity)
}
