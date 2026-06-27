package com.saglik.data.repository

import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.StepsDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.database.entity.ExerciseSessionEntity
import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.database.entity.StepsEntryEntity
import com.saglik.core.database.entity.WeightEntryEntity
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.model.DataSource
import com.saglik.core.model.HealthConnectExerciseSessionSnapshot
import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectStepsRecordSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultHealthConnectSyncRepositoryTest {

    @Test
    fun newHealthConnectWeightRecordInsertsOneRow() = runBlocking {
        val weightDao = FakeWeightDao()
        val repository = repository(weightDao = weightDao)

        val count = repository.importWeightRecords(
            records = listOf(weightSnapshot("weight-1", weightKg = 72.5f)),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(1, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(1, weightDao.entries.size)
        assertEquals(DataSource.HEALTH_CONNECT.name, weightDao.entries.single().source)
        assertEquals("weight-1", weightDao.entries.single().sourceRecordId)
        assertEquals(SyncAt, weightDao.entries.single().lastSyncedAt)
    }

    @Test
    fun sameHealthConnectWeightRecordImportedTwiceDoesNotDuplicate() = runBlocking {
        val weightDao = FakeWeightDao()
        val repository = repository(weightDao = weightDao)
        val record = weightSnapshot("weight-1", weightKg = 72.5f)

        repository.importWeightRecords(listOf(record), SyncAt)
        val secondCount = repository.importWeightRecords(listOf(record), SyncAt + 1L)

        assertEquals(0, secondCount.inserted)
        assertEquals(1, secondCount.updated)
        assertEquals(1, weightDao.entries.size)
        assertEquals("id-1", weightDao.entries.single().id)
        assertEquals(SyncAt + 1L, weightDao.entries.single().lastSyncedAt)
    }

    @Test
    fun updatedHealthConnectWeightRecordUpdatesExistingRow() = runBlocking {
        val weightDao = FakeWeightDao()
        val repository = repository(weightDao = weightDao)

        repository.importWeightRecords(listOf(weightSnapshot("weight-1", weightKg = 72.5f)), SyncAt)
        repository.importWeightRecords(listOf(weightSnapshot("weight-1", weightKg = 73.1f)), SyncAt + 1L)

        assertEquals(1, weightDao.entries.size)
        assertEquals(73.1f, weightDao.entries.single().weightKg)
        assertEquals("id-1", weightDao.entries.single().id)
    }

    @Test
    fun manualWeightRecordsAreNotModified() = runBlocking {
        val weightDao = FakeWeightDao(
            entries = mutableListOf(
                WeightEntryEntity(
                    id = "manual-1",
                    weightKg = 70f,
                    recordedAt = 10L,
                    source = DataSource.MANUAL.name,
                    note = "manual",
                    sourceRecordId = "weight-1",
                    sourcePackageName = "com.example.source",
                ),
            ),
        )
        val repository = repository(weightDao = weightDao)

        repository.importWeightRecords(listOf(weightSnapshot("weight-1", weightKg = 72.5f)), SyncAt)

        assertEquals(2, weightDao.entries.size)
        assertEquals(70f, weightDao.entries.first { it.id == "manual-1" }.weightKg)
        assertEquals(DataSource.MANUAL.name, weightDao.entries.first { it.id == "manual-1" }.source)
    }

    @Test
    fun newHealthConnectSleepSessionInsertsOneRow() = runBlocking {
        val sleepDao = FakeSleepDao()
        val repository = repository(sleepDao = sleepDao)

        val count = repository.importSleepSessionRecords(
            records = listOf(sleepSnapshot("sleep-1")),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(1, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(1, sleepDao.entries.size)
        assertEquals(DataSource.HEALTH_CONNECT.name, sleepDao.entries.single().source)
        assertEquals("sleep-1", sleepDao.entries.single().sourceRecordId)
    }

    @Test
    fun sameHealthConnectSleepSessionImportedTwiceDoesNotDuplicate() = runBlocking {
        val sleepDao = FakeSleepDao()
        val repository = repository(sleepDao = sleepDao)
        val record = sleepSnapshot("sleep-1")

        repository.importSleepSessionRecords(listOf(record), SyncAt)
        val secondCount = repository.importSleepSessionRecords(listOf(record), SyncAt + 1L)

        assertEquals(0, secondCount.inserted)
        assertEquals(1, secondCount.updated)
        assertEquals(1, sleepDao.entries.size)
        assertEquals("id-1", sleepDao.entries.single().id)
    }

    @Test
    fun invalidHealthConnectSleepSessionIsSkipped() = runBlocking {
        val sleepDao = FakeSleepDao()
        val repository = repository(sleepDao = sleepDao)

        val count = repository.importSleepSessionRecords(
            records = listOf(
                sleepSnapshot(
                    id = "sleep-1",
                    startTimeMillis = 10_000L,
                    endTimeMillis = 10_000L,
                    durationMinutes = 0,
                ),
            ),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(0, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(1, count.skipped)
        assertEquals(0, sleepDao.entries.size)
    }

    @Test
    fun manualSleepRecordsAreNotModified() = runBlocking {
        val sleepDao = FakeSleepDao(
            entries = mutableListOf(
                SleepEntryEntity(
                    id = "manual-1",
                    startTime = 10L,
                    endTime = 20L,
                    durationMinutes = 10,
                    quality = null,
                    source = DataSource.MANUAL.name,
                    note = "manual",
                    sourceRecordId = "sleep-1",
                    sourcePackageName = "com.example.source",
                ),
            ),
        )
        val repository = repository(sleepDao = sleepDao)

        repository.importSleepSessionRecords(listOf(sleepSnapshot("sleep-1")), SyncAt)

        assertEquals(2, sleepDao.entries.size)
        assertEquals(10, sleepDao.entries.first { it.id == "manual-1" }.durationMinutes)
        assertEquals(DataSource.MANUAL.name, sleepDao.entries.first { it.id == "manual-1" }.source)
    }

    @Test
    fun newHealthConnectStepsRecordInsertsOneRow() = runBlocking {
        val stepsDao = FakeStepsDao()
        val repository = repository(stepsDao = stepsDao)

        val count = repository.importStepsRecords(
            records = listOf(stepsSnapshot("steps-1", count = 1_200L)),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(1, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(0, count.skipped)
        assertEquals(1, stepsDao.entries.size)
        assertEquals(DataSource.HEALTH_CONNECT.name, stepsDao.entries.single().source)
        assertEquals("steps-1", stepsDao.entries.single().sourceRecordId)
        assertEquals(1_200L, stepsDao.entries.single().count)
        assertEquals(SyncAt, stepsDao.entries.single().lastSyncedAt)
    }

    @Test
    fun sameHealthConnectStepsRecordImportedTwiceDoesNotDuplicate() = runBlocking {
        val stepsDao = FakeStepsDao()
        val repository = repository(stepsDao = stepsDao)
        val record = stepsSnapshot("steps-1", count = 1_200L)

        repository.importStepsRecords(listOf(record), SyncAt)
        val secondCount = repository.importStepsRecords(listOf(record.copy(count = 1_400L)), SyncAt + 1L)

        assertEquals(0, secondCount.inserted)
        assertEquals(1, secondCount.updated)
        assertEquals(0, secondCount.skipped)
        assertEquals(1, stepsDao.entries.size)
        assertEquals("id-1", stepsDao.entries.single().id)
        assertEquals(1_400L, stepsDao.entries.single().count)
        assertEquals(SyncAt + 1L, stepsDao.entries.single().lastSyncedAt)
    }

    @Test
    fun invalidHealthConnectStepsRecordsAreSkipped() = runBlocking {
        val stepsDao = FakeStepsDao()
        val repository = repository(stepsDao = stepsDao)

        val count = repository.importStepsRecords(
            records = listOf(
                stepsSnapshot(id = "", count = 1_200L),
                stepsSnapshot(
                    id = "steps-1",
                    startTimeMillis = 10_000L,
                    endTimeMillis = 10_000L,
                    count = 1_200L,
                ),
                stepsSnapshot(id = "steps-2", count = 0L),
            ),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(0, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(3, count.skipped)
        assertEquals(0, stepsDao.entries.size)
    }

    @Test
    fun manualStepsRecordsAreNotModified() = runBlocking {
        val stepsDao = FakeStepsDao(
            entries = mutableListOf(
                StepsEntryEntity(
                    id = "manual-1",
                    startTime = 10L,
                    endTime = 20L,
                    count = 300L,
                    source = DataSource.MANUAL.name,
                    note = "manual",
                    sourceRecordId = "steps-1",
                    sourcePackageName = "com.example.source",
                ),
            ),
        )
        val repository = repository(stepsDao = stepsDao)

        repository.importStepsRecords(listOf(stepsSnapshot("steps-1", count = 1_200L)), SyncAt)

        assertEquals(2, stepsDao.entries.size)
        assertEquals(300L, stepsDao.entries.first { it.id == "manual-1" }.count)
        assertEquals(DataSource.MANUAL.name, stepsDao.entries.first { it.id == "manual-1" }.source)
    }

    @Test
    fun existingStepsNoteAndDeletedAtArePreservedWhenUpdating() = runBlocking {
        val stepsDao = FakeStepsDao(
            entries = mutableListOf(
                StepsEntryEntity(
                    id = "existing-1",
                    startTime = 10L,
                    endTime = 20L,
                    count = 300L,
                    source = DataSource.HEALTH_CONNECT.name,
                    note = "keep note",
                    sourceRecordId = "steps-1",
                    sourcePackageName = "com.example.source",
                    sourceAppName = "Old source",
                    createdAt = 111L,
                    updatedAt = 222L,
                    lastSyncedAt = 333L,
                    deletedAt = 444L,
                ),
            ),
        )
        val repository = repository(stepsDao = stepsDao)

        val count = repository.importStepsRecords(
            listOf(
                stepsSnapshot(
                    id = "steps-1",
                    count = 1_200L,
                    lastModifiedAtMillis = 555L,
                ),
            ),
            SyncAt,
        )

        assertEquals(0, count.inserted)
        assertEquals(1, count.updated)
        val entry = stepsDao.entries.single()
        assertEquals("existing-1", entry.id)
        assertEquals("keep note", entry.note)
        assertEquals(111L, entry.createdAt)
        assertEquals(555L, entry.updatedAt)
        assertEquals(SyncAt, entry.lastSyncedAt)
        assertEquals(444L, entry.deletedAt)
    }

    @Test
    fun newHealthConnectExerciseSessionInsertsOneRow() = runBlocking {
        val exerciseDao = FakeExerciseDao()
        val repository = repository(exerciseDao = exerciseDao)

        val count = repository.importExerciseSessionRecords(
            records = listOf(exerciseSnapshot("exercise-1", exerciseType = 79)),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(1, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(0, count.skipped)
        assertEquals(1, exerciseDao.entries.size)
        val entry = exerciseDao.entries.single()
        assertEquals(DataSource.HEALTH_CONNECT.name, entry.source)
        assertEquals("exercise-1", entry.sourceRecordId)
        assertEquals("WALKING", entry.exerciseType)
        assertEquals("Morning session", entry.title)
        assertEquals("Imported note", entry.notes)
        assertEquals(SyncAt, entry.lastSyncedAt)
    }

    @Test
    fun sameHealthConnectExerciseSessionImportedTwiceDoesNotDuplicate() = runBlocking {
        val exerciseDao = FakeExerciseDao()
        val repository = repository(exerciseDao = exerciseDao)
        val record = exerciseSnapshot("exercise-1", exerciseType = 56)

        repository.importExerciseSessionRecords(listOf(record), SyncAt)
        val secondCount = repository.importExerciseSessionRecords(
            listOf(record.copy(durationMinutes = 90, exerciseType = 8)),
            SyncAt + 1L,
        )

        assertEquals(0, secondCount.inserted)
        assertEquals(1, secondCount.updated)
        assertEquals(0, secondCount.skipped)
        assertEquals(1, exerciseDao.entries.size)
        val entry = exerciseDao.entries.single()
        assertEquals("id-1", entry.id)
        assertEquals(90, entry.durationMinutes)
        assertEquals("CYCLING", entry.exerciseType)
        assertEquals(SyncAt + 1L, entry.lastSyncedAt)
    }

    @Test
    fun invalidHealthConnectExerciseSessionsAreSkipped() = runBlocking {
        val exerciseDao = FakeExerciseDao()
        val repository = repository(exerciseDao = exerciseDao)

        val count = repository.importExerciseSessionRecords(
            records = listOf(
                exerciseSnapshot(id = ""),
                exerciseSnapshot(
                    id = "exercise-1",
                    startTimeMillis = 10_000L,
                    endTimeMillis = 10_000L,
                    durationMinutes = 0,
                ),
                exerciseSnapshot(id = "exercise-2", durationMinutes = 0),
            ),
            lastSyncedAtMillis = SyncAt,
        )

        assertEquals(0, count.inserted)
        assertEquals(0, count.updated)
        assertEquals(3, count.skipped)
        assertEquals(0, exerciseDao.entries.size)
    }

    @Test
    fun manualExerciseSessionsAreNotModified() = runBlocking {
        val exerciseDao = FakeExerciseDao(
            entries = mutableListOf(
                ExerciseSessionEntity(
                    id = "manual-1",
                    startTime = 10L,
                    endTime = 20L,
                    durationMinutes = 10,
                    exerciseType = "YOGA",
                    title = "Manual",
                    notes = "manual",
                    source = DataSource.MANUAL.name,
                    sourceRecordId = "exercise-1",
                    sourcePackageName = "com.example.source",
                ),
            ),
        )
        val repository = repository(exerciseDao = exerciseDao)

        repository.importExerciseSessionRecords(listOf(exerciseSnapshot("exercise-1")), SyncAt)

        assertEquals(2, exerciseDao.entries.size)
        val manualEntry = exerciseDao.entries.first { it.id == "manual-1" }
        assertEquals("YOGA", manualEntry.exerciseType)
        assertEquals("manual", manualEntry.notes)
        assertEquals(DataSource.MANUAL.name, manualEntry.source)
    }

    @Test
    fun existingExerciseNotesAndDeletedAtArePreservedWhenUpdating() = runBlocking {
        val exerciseDao = FakeExerciseDao(
            entries = mutableListOf(
                ExerciseSessionEntity(
                    id = "existing-1",
                    startTime = 10L,
                    endTime = 20L,
                    durationMinutes = 10,
                    exerciseType = "RUNNING",
                    title = "Existing",
                    notes = "keep notes",
                    source = DataSource.HEALTH_CONNECT.name,
                    sourceRecordId = "exercise-1",
                    sourcePackageName = "com.example.source",
                    sourceAppName = "Old source",
                    createdAt = 111L,
                    updatedAt = 222L,
                    lastSyncedAt = 333L,
                    deletedAt = 444L,
                ),
            ),
        )
        val repository = repository(exerciseDao = exerciseDao)

        val count = repository.importExerciseSessionRecords(
            listOf(
                exerciseSnapshot(
                    id = "exercise-1",
                    exerciseType = 999,
                    title = "Updated",
                    notes = "source notes",
                    lastModifiedAtMillis = 555L,
                ),
            ),
            SyncAt,
        )

        assertEquals(0, count.inserted)
        assertEquals(1, count.updated)
        val entry = exerciseDao.entries.single()
        assertEquals("existing-1", entry.id)
        assertEquals("OTHER", entry.exerciseType)
        assertEquals("Updated", entry.title)
        assertEquals("keep notes", entry.notes)
        assertEquals(111L, entry.createdAt)
        assertEquals(555L, entry.updatedAt)
        assertEquals(SyncAt, entry.lastSyncedAt)
        assertEquals(444L, entry.deletedAt)
    }

    private fun repository(
        weightDao: FakeWeightDao = FakeWeightDao(),
        sleepDao: FakeSleepDao = FakeSleepDao(),
        stepsDao: FakeStepsDao = FakeStepsDao(),
        exerciseDao: FakeExerciseDao = FakeExerciseDao(),
    ): DefaultHealthConnectSyncRepository {
        var nextId = 0
        return DefaultHealthConnectSyncRepository(
            dataSource = FakeHealthConnectDataSource(),
            weightDao = weightDao,
            sleepDao = sleepDao,
            stepsDao = stepsDao,
            exerciseDao = exerciseDao,
            idFactory = {
                nextId += 1
                "id-$nextId"
            },
        )
    }

    private fun weightSnapshot(
        id: String,
        weightKg: Float,
    ): HealthConnectWeightRecordSnapshot =
        HealthConnectWeightRecordSnapshot(
            healthConnectId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            weightKg = weightKg,
            recordedAtMillis = 1_700_000_000_000L,
            lastModifiedAtMillis = 1_700_000_100_000L,
        )

    private fun sleepSnapshot(
        id: String,
        startTimeMillis: Long = 1_700_000_000_000L,
        endTimeMillis: Long = 1_700_028_800_000L,
        durationMinutes: Int = 480,
    ): HealthConnectSleepSessionSnapshot =
        HealthConnectSleepSessionSnapshot(
            healthConnectId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            durationMinutes = durationMinutes,
            lastModifiedAtMillis = 1_700_030_000_000L,
        )

    private fun stepsSnapshot(
        id: String,
        startTimeMillis: Long = 1_700_000_000_000L,
        endTimeMillis: Long = 1_700_003_600_000L,
        count: Long = 1_200L,
        lastModifiedAtMillis: Long? = 1_700_030_000_000L,
    ): HealthConnectStepsRecordSnapshot =
        HealthConnectStepsRecordSnapshot(
            healthConnectId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            count = count,
            lastModifiedAtMillis = lastModifiedAtMillis,
        )

    private fun exerciseSnapshot(
        id: String,
        startTimeMillis: Long = 1_700_000_000_000L,
        endTimeMillis: Long = 1_700_003_600_000L,
        durationMinutes: Int = 60,
        exerciseType: Int = 79,
        title: String? = "Morning session",
        notes: String? = "Imported note",
        lastModifiedAtMillis: Long? = 1_700_030_000_000L,
    ): HealthConnectExerciseSessionSnapshot =
        HealthConnectExerciseSessionSnapshot(
            healthConnectId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            durationMinutes = durationMinutes,
            exerciseType = exerciseType,
            title = title,
            notes = notes,
            lastModifiedAtMillis = lastModifiedAtMillis,
        )

    private class FakeWeightDao(
        val entries: MutableList<WeightEntryEntity> = mutableListOf(),
    ) : WeightDao {
        override suspend fun insertWeightEntry(entry: WeightEntryEntity) {
            entries.removeAll { it.id == entry.id }
            entries += entry
        }

        override fun observeLatestWeightEntry(): Flow<WeightEntryEntity?> =
            flowOf(entries.maxByOrNull { it.recordedAt })

        override fun observeWeightEntries(): Flow<List<WeightEntryEntity>> =
            flowOf(entries.sortedByDescending { it.recordedAt })

        override suspend fun findByExternalIdentity(
            source: String,
            sourcePackageName: String?,
            sourceRecordId: String,
        ): WeightEntryEntity? =
            entries.firstOrNull {
                it.source == source &&
                    it.sourcePackageName == sourcePackageName &&
                    it.sourceRecordId == sourceRecordId
            }

        override fun observeBySource(source: String): Flow<List<WeightEntryEntity>> =
            flowOf(entries.filter { it.source == source }.sortedByDescending { it.recordedAt })
    }

    private class FakeSleepDao(
        val entries: MutableList<SleepEntryEntity> = mutableListOf(),
    ) : SleepDao {
        override fun observeAllSleepEntries(): Flow<List<SleepEntryEntity>> =
            flowOf(entries.sortedByDescending { it.endTime })

        override fun observeLatestSleepEntry(): Flow<SleepEntryEntity?> =
            flowOf(entries.maxByOrNull { it.endTime })

        override fun observeSleepEntriesBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<SleepEntryEntity>> =
            flowOf(
                entries
                    .filter { it.endTime >= startInclusive && it.endTime < endExclusive }
                    .sortedBy { it.startTime },
            )

        override suspend fun insertSleepEntry(entry: SleepEntryEntity) {
            entries.removeAll { it.id == entry.id }
            entries += entry
        }

        override suspend fun findByExternalIdentity(
            source: String,
            sourcePackageName: String?,
            sourceRecordId: String,
        ): SleepEntryEntity? =
            entries.firstOrNull {
                it.source == source &&
                    it.sourcePackageName == sourcePackageName &&
                    it.sourceRecordId == sourceRecordId
            }

        override fun observeBySource(source: String): Flow<List<SleepEntryEntity>> =
            flowOf(entries.filter { it.source == source }.sortedByDescending { it.endTime })

        override suspend fun deleteSleepEntry(entry: SleepEntryEntity) {
            entries.removeAll { it.id == entry.id }
        }
    }

    private class FakeStepsDao(
        val entries: MutableList<StepsEntryEntity> = mutableListOf(),
    ) : StepsDao {
        override suspend fun insertStepsEntry(entry: StepsEntryEntity) {
            entries.removeAll { it.id == entry.id }
            entries += entry
        }

        override fun observeStepsEntries(): Flow<List<StepsEntryEntity>> =
            flowOf(entries.sortedByDescending { it.endTime })

        override fun observeStepsEntriesBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<StepsEntryEntity>> =
            flowOf(
                entries
                    .filter { it.startTime >= startInclusive && it.startTime < endExclusive }
                    .sortedBy { it.startTime },
            )

        override suspend fun findByExternalIdentity(
            source: String,
            sourcePackageName: String?,
            sourceRecordId: String,
        ): StepsEntryEntity? =
            entries.firstOrNull {
                it.source == source &&
                    it.sourcePackageName == sourcePackageName &&
                    it.sourceRecordId == sourceRecordId
            }

        override fun observeBySource(source: String): Flow<List<StepsEntryEntity>> =
            flowOf(entries.filter { it.source == source }.sortedByDescending { it.endTime })
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

    private class FakeHealthConnectDataSource : HealthConnectDataSource {
        override fun getRequiredPermissions(): Set<String> = emptySet()

        override suspend fun getAvailability(): HealthConnectAvailability =
            HealthConnectAvailability.Available

        override suspend fun getPermissionStatus(): HealthConnectPermissionStatus =
            HealthConnectPermissionStatus.from(emptySet(), emptySet())

        override suspend fun readWeightRecords(
            startTimeMillis: Long,
            endTimeMillis: Long,
        ): List<HealthConnectWeightRecordSnapshot> = emptyList()

        override suspend fun readSleepSessionRecords(
            startTimeMillis: Long,
            endTimeMillis: Long,
        ): List<HealthConnectSleepSessionSnapshot> = emptyList()

        override suspend fun readStepsRecords(
            startTimeMillis: Long,
            endTimeMillis: Long,
        ): List<HealthConnectStepsRecordSnapshot> = emptyList()

        override suspend fun readExerciseSessionRecords(
            startTimeMillis: Long,
            endTimeMillis: Long,
        ): List<HealthConnectExerciseSessionSnapshot> = emptyList()
    }

    private companion object {
        private const val SyncAt = 2_000_000_000_000L
    }
}
