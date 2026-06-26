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

    @Query(
        """
        SELECT * FROM weight_entries
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
    ): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries WHERE source = :source ORDER BY recordedAt DESC")
    fun observeBySource(source: String): Flow<List<WeightEntryEntity>>
}
