package com.saglik.core.healthconnect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HealthConnectSnapshotMappersTest {

    @Test
    fun weightRecordLikeInputMapsToSnapshot() {
        val snapshot = weightRecordSnapshot(
            healthConnectId = "weight-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            weightKg = 72.5f,
            recordedAtMillis = 1_700_000_000_000L,
            lastModifiedAtMillis = 1_700_000_100_000L,
        )

        requireNotNull(snapshot)
        assertEquals("weight-1", snapshot.healthConnectId)
        assertEquals("com.example.source", snapshot.sourcePackageName)
        assertEquals(72.5f, snapshot.weightKg)
        assertEquals(1_700_000_000_000L, snapshot.recordedAtMillis)
        assertEquals(1_700_000_100_000L, snapshot.lastModifiedAtMillis)
    }

    @Test
    fun sleepSessionLikeInputMapsToSnapshot() {
        val snapshot = sleepSessionSnapshot(
            healthConnectId = "sleep-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_028_800_000L,
            lastModifiedAtMillis = 1_700_030_000_000L,
        )

        requireNotNull(snapshot)
        assertEquals("sleep-1", snapshot.healthConnectId)
        assertEquals("com.example.source", snapshot.sourcePackageName)
        assertEquals(1_700_000_000_000L, snapshot.startTimeMillis)
        assertEquals(1_700_028_800_000L, snapshot.endTimeMillis)
        assertEquals(480, snapshot.durationMinutes)
        assertEquals(1_700_030_000_000L, snapshot.lastModifiedAtMillis)
    }

    @Test
    fun invalidSleepSessionIsRejectedSafely() {
        val snapshot = sleepSessionSnapshot(
            healthConnectId = "sleep-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_000_000_000L,
            lastModifiedAtMillis = null,
        )

        assertNull(snapshot)
    }

    @Test
    fun blankHealthConnectIdIsRejectedSafely() {
        val weightSnapshot = weightRecordSnapshot(
            healthConnectId = "",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            weightKg = 72.5f,
            recordedAtMillis = 1_700_000_000_000L,
            lastModifiedAtMillis = null,
        )
        val sleepSnapshot = sleepSessionSnapshot(
            healthConnectId = "",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_028_800_000L,
            lastModifiedAtMillis = null,
        )

        assertNull(weightSnapshot)
        assertNull(sleepSnapshot)
    }
}
