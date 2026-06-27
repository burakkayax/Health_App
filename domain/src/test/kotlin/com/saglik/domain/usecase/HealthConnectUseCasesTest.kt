package com.saglik.domain.usecase

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import com.saglik.domain.repository.HealthConnectRepository
import com.saglik.domain.repository.HealthConnectSyncRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectUseCasesTest {
    private val readWeight = "android.permission.health.READ_WEIGHT"
    private val readSleep = "android.permission.health.READ_SLEEP"
    private val requiredPermissions = setOf(readWeight, readSleep)

    @Test
    fun fakeRepositoryReportsUnsupportedAndMissingPermissions() = runBlocking {
        val repository = FakeHealthConnectRepository(
            requiredPermissions = requiredPermissions,
            availability = HealthConnectAvailability.Unsupported,
            grantedPermissions = emptySet(),
        )

        assertEquals(
            HealthConnectAvailability.Unsupported,
            CheckHealthConnectAvailabilityUseCase(repository)(),
        )

        val status = GetHealthConnectPermissionStatusUseCase(repository)()

        assertFalse(status.allRequiredGranted)
        assertEquals(requiredPermissions, status.requiredPermissions)
        assertEquals(requiredPermissions, status.missingPermissions)
    }

    @Test
    fun unsupportedAvailabilityReturnsNoSyncOutcome() = runBlocking {
        val syncRepository = FakeHealthConnectSyncRepository()
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.Unsupported,
                grantedPermissions = requiredPermissions,
            ),
            syncRepository = syncRepository,
            nowMillis = { Now },
        )

        val outcome = useCase()

        assertEquals(HealthConnectSyncOutcome.Unsupported, outcome)
        assertEquals(0, syncRepository.weightReadCount)
        assertEquals(0, syncRepository.sleepReadCount)
    }

    @Test
    fun providerUpdateRequiredReturnsNoSyncOutcome() = runBlocking {
        val syncRepository = FakeHealthConnectSyncRepository()
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.ProviderUpdateRequired,
                grantedPermissions = requiredPermissions,
            ),
            syncRepository = syncRepository,
            nowMillis = { Now },
        )

        val outcome = useCase()

        assertEquals(HealthConnectSyncOutcome.ProviderUpdateRequired, outcome)
        assertEquals(0, syncRepository.weightReadCount)
        assertEquals(0, syncRepository.sleepReadCount)
    }

    @Test
    fun missingPermissionsReturnNoSyncOutcome() = runBlocking {
        val syncRepository = FakeHealthConnectSyncRepository()
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = setOf(readWeight),
            ),
            syncRepository = syncRepository,
            nowMillis = { Now },
        )

        val outcome = useCase()

        assertEquals(HealthConnectSyncOutcome.PermissionMissing, outcome)
        assertEquals(0, syncRepository.weightReadCount)
        assertEquals(0, syncRepository.sleepReadCount)
    }

    @Test
    fun grantedPermissionsReadAndImportRecordsWithinThirtyDayWindow() = runBlocking {
        val syncRepository = FakeHealthConnectSyncRepository(
            weightRecords = listOf(weightSnapshot("weight-1")),
            sleepRecords = listOf(sleepSnapshot("sleep-1")),
        )
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = requiredPermissions,
            ),
            syncRepository = syncRepository,
            nowMillis = { Now },
        )

        val outcome = useCase()

        assertTrue(outcome is HealthConnectSyncOutcome.Success)
        val result = (outcome as HealthConnectSyncOutcome.Success).result
        assertEquals(1, result.weightInserted)
        assertEquals(1, result.sleepInserted)
        assertEquals(Now - ThirtyDaysMillis, syncRepository.lastWeightStartTimeMillis)
        assertEquals(Now, syncRepository.lastWeightEndTimeMillis)
        assertEquals(Now - ThirtyDaysMillis, syncRepository.lastSleepStartTimeMillis)
        assertEquals(Now, syncRepository.lastSleepEndTimeMillis)
    }

    @Test
    fun emptyHealthConnectResultReturnsNoData() = runBlocking {
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = requiredPermissions,
            ),
            syncRepository = FakeHealthConnectSyncRepository(),
            nowMillis = { Now },
        )

        val outcome = useCase()

        assertTrue(outcome is HealthConnectSyncOutcome.NoData)
        val result = (outcome as HealthConnectSyncOutcome.NoData).result
        assertEquals(0, result.weightInserted)
        assertEquals(0, result.weightUpdated)
        assertEquals(0, result.sleepInserted)
        assertEquals(0, result.sleepUpdated)
        assertEquals(0, result.skipped)
    }

    @Test
    fun readFailureReturnsSafeFailure() = runBlocking {
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = requiredPermissions,
            ),
            syncRepository = FakeHealthConnectSyncRepository(throwOnRead = true),
            nowMillis = { Now },
        )

        val outcome = useCase()

        assertEquals(HealthConnectSyncOutcome.Failed, outcome)
    }

    @Test
    fun secondSyncWithSameRecordsReportsUpdatesWithoutDuplicates() = runBlocking {
        val syncRepository = FakeHealthConnectSyncRepository(
            weightRecords = listOf(weightSnapshot("weight-1")),
            sleepRecords = listOf(sleepSnapshot("sleep-1")),
        )
        val useCase = SyncHealthConnectWeightAndSleepUseCase(
            healthConnectRepository = FakeHealthConnectRepository(
                requiredPermissions = requiredPermissions,
                availability = HealthConnectAvailability.Available,
                grantedPermissions = requiredPermissions,
            ),
            syncRepository = syncRepository,
            nowMillis = { Now },
        )

        useCase()
        val secondOutcome = useCase()

        assertTrue(secondOutcome is HealthConnectSyncOutcome.Success)
        val result = (secondOutcome as HealthConnectSyncOutcome.Success).result
        assertEquals(0, result.weightInserted)
        assertEquals(1, result.weightUpdated)
        assertEquals(0, result.sleepInserted)
        assertEquals(1, result.sleepUpdated)
        assertEquals(1, syncRepository.importedWeightIds.size)
        assertEquals(1, syncRepository.importedSleepIds.size)
    }

    private class FakeHealthConnectRepository(
        private val requiredPermissions: Set<String>,
        private val availability: HealthConnectAvailability,
        private val grantedPermissions: Set<String>,
    ) : HealthConnectRepository {
        override fun getRequiredPermissions(): Set<String> = requiredPermissions

        override suspend fun getAvailability(): HealthConnectAvailability = availability

        override suspend fun getPermissionStatus(): HealthConnectPermissionStatus =
            HealthConnectPermissionStatus.from(
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions,
            )
    }

    private class FakeHealthConnectSyncRepository(
        private val weightRecords: List<HealthConnectWeightRecordSnapshot> = emptyList(),
        private val sleepRecords: List<HealthConnectSleepSessionSnapshot> = emptyList(),
        private val throwOnRead: Boolean = false,
    ) : HealthConnectSyncRepository {
        val importedWeightIds = mutableSetOf<String>()
        val importedSleepIds = mutableSetOf<String>()
        var weightReadCount = 0
        var sleepReadCount = 0
        var lastWeightStartTimeMillis: Long? = null
        var lastWeightEndTimeMillis: Long? = null
        var lastSleepStartTimeMillis: Long? = null
        var lastSleepEndTimeMillis: Long? = null

        override suspend fun readWeightRecords(
            startTimeMillis: Long,
            endTimeMillis: Long,
        ): List<HealthConnectWeightRecordSnapshot> {
            if (throwOnRead) {
                error("read failed")
            }
            weightReadCount += 1
            lastWeightStartTimeMillis = startTimeMillis
            lastWeightEndTimeMillis = endTimeMillis
            return weightRecords
        }

        override suspend fun readSleepSessionRecords(
            startTimeMillis: Long,
            endTimeMillis: Long,
        ): List<HealthConnectSleepSessionSnapshot> {
            if (throwOnRead) {
                error("read failed")
            }
            sleepReadCount += 1
            lastSleepStartTimeMillis = startTimeMillis
            lastSleepEndTimeMillis = endTimeMillis
            return sleepRecords
        }

        override suspend fun importWeightRecords(
            records: List<HealthConnectWeightRecordSnapshot>,
            lastSyncedAtMillis: Long,
        ): HealthConnectImportCount {
            var inserted = 0
            var updated = 0
            records.forEach { record ->
                if (importedWeightIds.add(record.healthConnectId)) {
                    inserted += 1
                } else {
                    updated += 1
                }
            }
            return HealthConnectImportCount(inserted = inserted, updated = updated, skipped = 0)
        }

        override suspend fun importSleepSessionRecords(
            records: List<HealthConnectSleepSessionSnapshot>,
            lastSyncedAtMillis: Long,
        ): HealthConnectImportCount {
            var inserted = 0
            var updated = 0
            records.forEach { record ->
                if (record.endTimeMillis <= record.startTimeMillis || record.durationMinutes <= 0) {
                    return@forEach
                }

                if (importedSleepIds.add(record.healthConnectId)) {
                    inserted += 1
                } else {
                    updated += 1
                }
            }
            return HealthConnectImportCount(inserted = inserted, updated = updated, skipped = 0)
        }
    }

    private fun weightSnapshot(id: String): HealthConnectWeightRecordSnapshot =
        HealthConnectWeightRecordSnapshot(
            healthConnectId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            weightKg = 72.5f,
            recordedAtMillis = Now - 1_000L,
            lastModifiedAtMillis = null,
        )

    private fun sleepSnapshot(id: String): HealthConnectSleepSessionSnapshot =
        HealthConnectSleepSessionSnapshot(
            healthConnectId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = Now - 30_000_000L,
            endTimeMillis = Now - 1_000L,
            durationMinutes = 499,
            lastModifiedAtMillis = null,
        )

    private companion object {
        private const val Now = 2_000_000_000_000L
        private const val ThirtyDaysMillis = 30L * 24L * 60L * 60L * 1_000L
    }
}
