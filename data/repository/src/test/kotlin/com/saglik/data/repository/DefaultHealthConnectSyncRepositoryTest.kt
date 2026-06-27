package com.saglik.data.repository

import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.database.entity.WeightEntryEntity
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.model.DataSource
import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
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

    private fun repository(
        weightDao: FakeWeightDao = FakeWeightDao(),
        sleepDao: FakeSleepDao = FakeSleepDao(),
    ): DefaultHealthConnectSyncRepository {
        var nextId = 0
        return DefaultHealthConnectSyncRepository(
            dataSource = FakeHealthConnectDataSource(),
            weightDao = weightDao,
            sleepDao = sleepDao,
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
    }

    private companion object {
        private const val SyncAt = 2_000_000_000_000L
    }
}
