@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectSyncWindow
import com.saglik.domain.repository.HealthConnectRepository
import com.saglik.domain.repository.HealthConnectSyncRepository
import kotlin.time.Clock

class SyncHealthConnectDataUseCase(
    private val healthConnectRepository: HealthConnectRepository,
    private val syncRepository: HealthConnectSyncRepository,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    suspend operator fun invoke(): HealthConnectSyncOutcome {
        val startedAtMillis = nowMillis()

        return runCatching {
            when (healthConnectRepository.getAvailability()) {
                HealthConnectAvailability.Available -> Unit
                HealthConnectAvailability.ProviderUpdateRequired -> {
                    return@runCatching HealthConnectSyncOutcome.ProviderUpdateRequired
                }
                HealthConnectAvailability.Unsupported -> {
                    return@runCatching HealthConnectSyncOutcome.Unsupported
                }
            }

            val permissionStatus = healthConnectRepository.getPermissionStatus()
            if (!permissionStatus.allRequiredGranted) {
                return@runCatching HealthConnectSyncOutcome.PermissionMissing
            }

            val syncWindow = syncWindowEndingAt(startedAtMillis)
            val weightRecords = syncRepository.readWeightRecords(
                startTimeMillis = syncWindow.startTimeMillis,
                endTimeMillis = syncWindow.endTimeMillis,
            )
            val sleepRecords = syncRepository.readSleepSessionRecords(
                startTimeMillis = syncWindow.startTimeMillis,
                endTimeMillis = syncWindow.endTimeMillis,
            )
            val stepsRecords = syncRepository.readStepsRecords(
                startTimeMillis = syncWindow.startTimeMillis,
                endTimeMillis = syncWindow.endTimeMillis,
            )
            val exerciseRecords = syncRepository.readExerciseSessionRecords(
                startTimeMillis = syncWindow.startTimeMillis,
                endTimeMillis = syncWindow.endTimeMillis,
            )

            val weightCount = syncRepository.importWeightRecords(
                records = weightRecords,
                lastSyncedAtMillis = startedAtMillis,
            )
            val sleepCount = syncRepository.importSleepSessionRecords(
                records = sleepRecords,
                lastSyncedAtMillis = startedAtMillis,
            )
            val stepsCount = syncRepository.importStepsRecords(
                records = stepsRecords,
                lastSyncedAtMillis = startedAtMillis,
            )
            val exerciseCount = syncRepository.importExerciseSessionRecords(
                records = exerciseRecords,
                lastSyncedAtMillis = startedAtMillis,
            )
            val finishedAtMillis = nowMillis()
            val result = HealthConnectSyncResult(
                weightInserted = weightCount.inserted,
                weightUpdated = weightCount.updated,
                sleepInserted = sleepCount.inserted,
                sleepUpdated = sleepCount.updated,
                stepsInserted = stepsCount.inserted,
                stepsUpdated = stepsCount.updated,
                exerciseInserted = exerciseCount.inserted,
                exerciseUpdated = exerciseCount.updated,
                skipped = weightCount.skipped +
                    sleepCount.skipped +
                    stepsCount.skipped +
                    exerciseCount.skipped,
                startedAtMillis = startedAtMillis,
                finishedAtMillis = finishedAtMillis,
            )

            if (result.hasImportedChanges || result.skipped > 0) {
                HealthConnectSyncOutcome.Success(result)
            } else {
                HealthConnectSyncOutcome.NoData(result)
            }
        }.getOrElse {
            HealthConnectSyncOutcome.Failed
        }
    }

    private fun syncWindowEndingAt(endTimeMillis: Long): HealthConnectSyncWindow =
        HealthConnectSyncWindow(
            startTimeMillis = endTimeMillis - SyncWindowMillis,
            endTimeMillis = endTimeMillis,
        )

    private companion object {
        private const val SyncWindowMillis = 30L * 24L * 60L * 60L * 1_000L
    }
}

sealed interface HealthConnectSyncOutcome {
    data class Success(val result: HealthConnectSyncResult) : HealthConnectSyncOutcome
    data class NoData(val result: HealthConnectSyncResult) : HealthConnectSyncOutcome
    data object PermissionMissing : HealthConnectSyncOutcome
    data object ProviderUpdateRequired : HealthConnectSyncOutcome
    data object Unsupported : HealthConnectSyncOutcome
    data object Failed : HealthConnectSyncOutcome
}

data class HealthConnectSyncResult(
    val weightInserted: Int,
    val weightUpdated: Int,
    val sleepInserted: Int,
    val sleepUpdated: Int,
    val stepsInserted: Int,
    val stepsUpdated: Int,
    val exerciseInserted: Int,
    val exerciseUpdated: Int,
    val skipped: Int,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
) {
    val hasImportedChanges: Boolean =
        weightInserted > 0 ||
            weightUpdated > 0 ||
            sleepInserted > 0 ||
            sleepUpdated > 0 ||
            stepsInserted > 0 ||
            stepsUpdated > 0 ||
            exerciseInserted > 0 ||
            exerciseUpdated > 0
}

data class HealthConnectImportCount(
    val inserted: Int,
    val updated: Int,
    val skipped: Int,
)
