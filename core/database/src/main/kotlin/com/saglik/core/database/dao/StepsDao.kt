package com.saglik.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saglik.core.database.entity.StepsEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepsEntry(entry: StepsEntryEntity)

    @Query("SELECT * FROM steps_entries ORDER BY endTime DESC")
    fun observeStepsEntries(): Flow<List<StepsEntryEntity>>

    @Query(
        """
        SELECT * FROM steps_entries
        WHERE startTime >= :startInclusive AND startTime < :endExclusive
        ORDER BY startTime ASC
        """,
    )
    fun observeStepsEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<StepsEntryEntity>>

    @Query(
        """
        SELECT * FROM steps_entries
        WHERE source = :source
            AND sourceRecordId = :sourceRecordId
            AND (
                (:sourcePackageName IS NULL AND sourcePackageName IS NULL)
                OR sourcePackageName = :sourcePackageName
            )
        LIMIT 1
        """,
    )
    suspend fun findByExternalIdentity(
        source: String,
        sourcePackageName: String?,
        sourceRecordId: String,
    ): StepsEntryEntity?

    @Query("SELECT * FROM steps_entries WHERE source = :source ORDER BY endTime DESC")
    fun observeBySource(source: String): Flow<List<StepsEntryEntity>>
}
