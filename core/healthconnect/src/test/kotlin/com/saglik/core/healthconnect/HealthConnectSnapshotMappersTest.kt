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
    fun stepsRecordLikeInputMapsToSnapshot() {
        val snapshot = stepsRecordSnapshot(
            healthConnectId = "steps-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_003_600_000L,
            count = 1_500L,
            lastModifiedAtMillis = 1_700_030_000_000L,
        )

        requireNotNull(snapshot)
        assertEquals("steps-1", snapshot.healthConnectId)
        assertEquals("com.example.source", snapshot.sourcePackageName)
        assertEquals(1_700_000_000_000L, snapshot.startTimeMillis)
        assertEquals(1_700_003_600_000L, snapshot.endTimeMillis)
        assertEquals(1_500L, snapshot.count)
        assertEquals(1_700_030_000_000L, snapshot.lastModifiedAtMillis)
    }

    @Test
    fun exerciseSessionRecordLikeInputMapsToSnapshot() {
        val snapshot = exerciseSessionSnapshot(
            healthConnectId = "exercise-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_003_600_000L,
            exerciseType = 79,
            title = "Walk",
            notes = "Easy pace",
            lastModifiedAtMillis = 1_700_030_000_000L,
        )

        requireNotNull(snapshot)
        assertEquals("exercise-1", snapshot.healthConnectId)
        assertEquals("com.example.source", snapshot.sourcePackageName)
        assertEquals(1_700_000_000_000L, snapshot.startTimeMillis)
        assertEquals(1_700_003_600_000L, snapshot.endTimeMillis)
        assertEquals(60, snapshot.durationMinutes)
        assertEquals(79, snapshot.exerciseType)
        assertEquals("Walk", snapshot.title)
        assertEquals("Easy pace", snapshot.notes)
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
        val stepsSnapshot = stepsRecordSnapshot(
            healthConnectId = "",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_003_600_000L,
            count = 1_500L,
            lastModifiedAtMillis = null,
        )
        val exerciseSnapshot = exerciseSessionSnapshot(
            healthConnectId = "",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_003_600_000L,
            exerciseType = 79,
            title = null,
            notes = null,
            lastModifiedAtMillis = null,
        )

        assertNull(weightSnapshot)
        assertNull(sleepSnapshot)
        assertNull(stepsSnapshot)
        assertNull(exerciseSnapshot)
    }

    @Test
    fun invalidStepsIntervalIsRejectedSafely() {
        val snapshot = stepsRecordSnapshot(
            healthConnectId = "steps-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_000_000_000L,
            count = 1_500L,
            lastModifiedAtMillis = null,
        )

        assertNull(snapshot)
    }

    @Test
    fun nonPositiveStepsCountIsRejectedSafely() {
        val zeroCountSnapshot = stepsRecordSnapshot(
            healthConnectId = "steps-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_003_600_000L,
            count = 0L,
            lastModifiedAtMillis = null,
        )
        val negativeCountSnapshot = stepsRecordSnapshot(
            healthConnectId = "steps-2",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_003_600_000L,
            count = -1L,
            lastModifiedAtMillis = null,
        )

        assertNull(zeroCountSnapshot)
        assertNull(negativeCountSnapshot)
    }

    @Test
    fun invalidExerciseIntervalIsRejectedSafely() {
        val snapshot = exerciseSessionSnapshot(
            healthConnectId = "exercise-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_000_000_000L,
            exerciseType = 79,
            title = null,
            notes = null,
            lastModifiedAtMillis = null,
        )

        assertNull(snapshot)
    }

    @Test
    fun subMinuteExerciseDurationIsRejectedSafely() {
        val snapshot = exerciseSessionSnapshot(
            healthConnectId = "exercise-1",
            sourcePackageName = "com.example.source",
            sourceAppName = null,
            startTimeMillis = 1_700_000_000_000L,
            endTimeMillis = 1_700_000_059_000L,
            exerciseType = 79,
            title = null,
            notes = null,
            lastModifiedAtMillis = null,
        )

        assertNull(snapshot)
    }
}
