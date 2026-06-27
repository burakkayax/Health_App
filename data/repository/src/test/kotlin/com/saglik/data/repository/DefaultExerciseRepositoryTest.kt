package com.saglik.data.repository

import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.entity.ExerciseSessionEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.ExerciseType
import com.saglik.domain.repository.AddExerciseSessionInput
import com.saglik.domain.usecase.AddExerciseSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultExerciseRepositoryTest {
    @Test
    fun observeExerciseSessionsMapsEntityToDomain() = runBlocking {
        val dao = FakeExerciseDao(
            entries = mutableListOf(
                ExerciseSessionEntity(
                    id = "exercise-1",
                    startTime = 1_000L,
                    endTime = 61_000L,
                    durationMinutes = 1,
                    exerciseType = "RUNNING",
                    title = "Run",
                    notes = "Track",
                    source = DataSource.HEALTH_CONNECT.name,
                    sourceRecordId = "hc-1",
                    sourcePackageName = "com.example.source",
                    sourceAppName = "Example Source",
                    createdAt = 100L,
                    updatedAt = 200L,
                    lastSyncedAt = 300L,
                    deletedAt = null,
                ),
            ),
        )
        val repository = DefaultExerciseRepository(dao)

        val session = repository.observeExerciseSessions().first().single()

        assertEquals("exercise-1", session.id)
        assertEquals(1_000L, session.startTimeMillis)
        assertEquals(61_000L, session.endTimeMillis)
        assertEquals(1, session.durationMinutes)
        assertEquals(ExerciseType.RUNNING, session.exerciseType)
        assertEquals("Run", session.title)
        assertEquals("Track", session.notes)
        assertEquals(DataSource.HEALTH_CONNECT, session.source)
        assertEquals("hc-1", session.sourceRecordId)
        assertEquals("com.example.source", session.sourcePackageName)
        assertEquals("Example Source", session.sourceAppName)
        assertEquals(100L, session.createdAt)
        assertEquals(200L, session.updatedAt)
        assertEquals(300L, session.lastSyncedAt)
        assertNull(session.deletedAt)
    }

    @Test
    fun addExerciseSessionUseCaseStoresManualExercise() = runBlocking {
        val dao = FakeExerciseDao()
        val repository = DefaultExerciseRepository(
            exerciseDao = dao,
            idFactory = { "manual-exercise-1" },
        )
        val useCase = AddExerciseSessionUseCase(repository)

        val saved = useCase(
            AddExerciseSessionInput(
                startTimeMillis = 1_000L,
                endTimeMillis = 121_000L,
                exerciseType = ExerciseType.YOGA,
                title = " Yoga ",
                notes = " Calm ",
            ),
        )

        assertEquals(true, saved)
        val entity = dao.entries.single()
        assertEquals("manual-exercise-1", entity.id)
        assertEquals(DataSource.MANUAL.name, entity.source)
        assertEquals("YOGA", entity.exerciseType)
        assertEquals("Yoga", entity.title)
        assertEquals("Calm", entity.notes)
        assertEquals(2, entity.durationMinutes)
        assertNull(entity.sourceRecordId)
        assertNull(entity.sourcePackageName)
        assertNull(entity.lastSyncedAt)
    }

    private class FakeExerciseDao(
        val entries: MutableList<ExerciseSessionEntity> = mutableListOf(),
    ) : ExerciseDao {
        override suspend fun insertExerciseSession(entry: ExerciseSessionEntity) {
            entries.removeAll { it.id == entry.id }
            entries += entry
        }

        override fun observeExerciseSessions(): Flow<List<ExerciseSessionEntity>> =
            flowOf(entries.sortedByDescending { it.endTime })

        override fun observeLatestExerciseSession(): Flow<ExerciseSessionEntity?> =
            flowOf(entries.maxByOrNull { it.endTime })

        override fun observeExerciseSessionsBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<ExerciseSessionEntity>> =
            flowOf(
                entries
                    .filter { it.endTime >= startInclusive && it.endTime < endExclusive }
                    .sortedBy { it.startTime },
            )

        override suspend fun findByExternalIdentity(
            source: String,
            sourcePackageName: String?,
            sourceRecordId: String,
        ): ExerciseSessionEntity? =
            entries.firstOrNull {
                it.source == source &&
                    it.sourcePackageName == sourcePackageName &&
                    it.sourceRecordId == sourceRecordId
            }

        override fun observeBySource(source: String): Flow<List<ExerciseSessionEntity>> =
            flowOf(entries.filter { it.source == source }.sortedByDescending { it.endTime })
    }
}
