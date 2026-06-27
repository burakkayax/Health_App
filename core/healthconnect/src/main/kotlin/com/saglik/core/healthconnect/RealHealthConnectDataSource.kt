package com.saglik.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectAvailabilityMapper
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectSdkStatus
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import java.time.Instant

class RealHealthConnectDataSource(
    context: Context,
) : HealthConnectDataSource {
    private val appContext = context.applicationContext

    override fun getRequiredPermissions(): Set<String> = HealthConnectPermissions.requiredPermissions

    override suspend fun getAvailability(): HealthConnectAvailability =
        HealthConnectAvailabilityMapper.fromSdkStatus(
            HealthConnectClient.getSdkStatus(appContext).toHealthConnectSdkStatus(),
        )

    override suspend fun getPermissionStatus(): HealthConnectPermissionStatus {
        if (getAvailability() != HealthConnectAvailability.Available) {
            return HealthConnectPermissionStatus.from(
                requiredPermissions = getRequiredPermissions(),
                grantedPermissions = emptySet(),
            )
        }

        val grantedPermissions = HealthConnectClient
            .getOrCreate(appContext)
            .permissionController
            .getGrantedPermissions()

        return HealthConnectPermissionStatus.from(
            requiredPermissions = getRequiredPermissions(),
            grantedPermissions = grantedPermissions,
        )
    }

    override suspend fun readWeightRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectWeightRecordSnapshot> {
        val client = HealthConnectClient.getOrCreate(appContext)
        val records = mutableListOf<HealthConnectWeightRecordSnapshot>()
        var pageToken: String? = null

        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = syncTimeRangeFilter(startTimeMillis, endTimeMillis),
                    pageToken = pageToken,
                ),
            )
            records += response.records.mapNotNull { it.toSnapshot() }
            pageToken = response.pageToken
        } while (pageToken != null)

        return records
    }

    override suspend fun readSleepSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectSleepSessionSnapshot> {
        val client = HealthConnectClient.getOrCreate(appContext)
        val records = mutableListOf<HealthConnectSleepSessionSnapshot>()
        var pageToken: String? = null

        do {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = syncTimeRangeFilter(startTimeMillis, endTimeMillis),
                    pageToken = pageToken,
                ),
            )
            records += response.records.mapNotNull { it.toSnapshot() }
            pageToken = response.pageToken
        } while (pageToken != null)

        return records
    }

    private fun Int.toHealthConnectSdkStatus(): HealthConnectSdkStatus =
        when (this) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectSdkStatus.SDK_AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                HealthConnectSdkStatus.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
            }
            else -> HealthConnectSdkStatus.SDK_UNAVAILABLE
        }

    private fun syncTimeRangeFilter(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): TimeRangeFilter =
        TimeRangeFilter.between(
            Instant.ofEpochMilli(startTimeMillis),
            Instant.ofEpochMilli(endTimeMillis),
        )
}
