@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.WeightEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.datetime.Instant

class ObserveWeightTrendSummaryUseCaseTest {
    @Test
    fun emptyWeightListIsSafe() {
        val summary = ObserveWeightTrendSummaryUseCase.buildSummary(emptyList())

        assertNull(summary.latestEntry)
        assertTrue(summary.chartPoints.isEmpty())
        assertNull(summary.highestKg)
        assertNull(summary.lowestKg)
        assertTrue(summary.history.isEmpty())
    }

    @Test
    fun singleWeightEntryIsSafe() {
        val entry = weight("first", 61f, 1_000)

        val summary = ObserveWeightTrendSummaryUseCase.buildSummary(listOf(entry))

        assertEquals(entry, summary.latestEntry)
        assertEquals(listOf(61f), summary.chartPoints.map { it.weightKg })
        assertEquals(61f, summary.highestKg ?: 0f, 0.001f)
        assertEquals(61f, summary.lowestKg ?: 0f, 0.001f)
    }

    @Test
    fun allTimeTrendStartsWithFirstAndEndsWithLatestEntry() {
        val first = weight("first", 70f, 1_000)
        val middle = weight("middle", 68f, 86_401_000)
        val latest = weight("latest", 61f, 172_802_000)

        val summary = ObserveWeightTrendSummaryUseCase.buildSummary(listOf(middle, latest, first))

        assertEquals(listOf(70f, 68f, 61f), summary.chartPoints.map { it.weightKg })
        assertEquals(latest, summary.latestEntry)
        assertEquals(70f, summary.highestKg ?: 0f, 0.001f)
        assertEquals(61f, summary.lowestKg ?: 0f, 0.001f)
        assertEquals(listOf(latest, middle, first), summary.history)
    }

    @Test
    fun highestAndLowestAreCalculatedFromVisibleTrendRange() {
        val entries = listOf(
            weight("a", 63.2f, 1_000),
            weight("b", 60.8f, 86_401_000),
            weight("c", 61.4f, 172_802_000),
        )

        val summary = ObserveWeightTrendSummaryUseCase.buildSummary(entries)

        assertEquals(63.2f, summary.highestKg ?: 0f, 0.001f)
        assertEquals(60.8f, summary.lowestKg ?: 0f, 0.001f)
    }

    @Test
    fun chartUsesAllEntriesChronologically() {
        val morning = weight("morning", 63f, 1_000)
        val evening = weight("evening", 62.5f, 2_000)
        val nextDay = weight("next", 62.1f, 86_401_000)

        val summary = ObserveWeightTrendSummaryUseCase.buildSummary(listOf(morning, nextDay, evening))

        assertEquals(listOf(63f, 62.5f, 62.1f), summary.chartPoints.map { it.weightKg })
        assertEquals(listOf(nextDay, evening, morning), summary.history)
    }

    private fun weight(id: String, weightKg: Float, epochMillis: Long): WeightEntry =
        WeightEntry(
            id = id,
            weightKg = weightKg,
            recordedAt = Instant.fromEpochMilliseconds(epochMillis),
            source = DataSource.MANUAL,
            note = null,
        )
}
