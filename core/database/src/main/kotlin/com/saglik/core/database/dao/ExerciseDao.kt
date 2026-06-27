package com.saglik.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saglik.core.database.entity.ExerciseSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSession(entry: ExerciseSessionEntity)

    @Query("SELECT * FROM exercise_sessions ORDER BY endTime DESC")
    fun observeExerciseSessions(): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions ORDER BY endTime DESC LIMIT 1")
    fun observeLatestExerciseSession(): Flow<ExerciseSessionEntity?>

    @Query(
        """
        SELECT * FROM exercise_sessions
        WHERE endTime >= :startInclusive AND endTime < :endExclusive
        ORDER BY startTime ASC
        """,
    )
    fun observeExerciseSessionsBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<ExerciseSessionEntity>>

    @Query(
        """
        SELECT * FROM exercise_sessions
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
    ): ExerciseSessionEntity?

    @Query("SELECT * FROM exercise_sessions WHERE source = :source ORDER BY endTime DESC")
    fun observeBySource(source: String): Flow<List<ExerciseSessionEntity>>
}
