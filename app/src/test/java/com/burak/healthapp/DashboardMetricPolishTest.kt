package com.burak.healthapp

import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.caffeine.buildCaffeineMonthRingDays
import com.burak.healthapp.feature.detail.smoking.buildSmokingDetailUiState
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.components.SmokingDashboardTone
import com.burak.healthapp.feature.today.components.smokingDashboardToneForCount
import com.burak.healthapp.feature.today.smokingStatusForCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class DashboardMetricPolishTest {
    @Test
    fun smokingStatusForCount_distinguishesSafeWarningAndDanger() {
        assertEquals(SmokingStatus.PASSIVE, smokingStatusForCount(count = 3, limit = 0))
        assertEquals(SmokingStatus.SAFE, smokingStatusForCount(count = 0, limit = 3))
        assertEquals(SmokingStatus.NEUTRAL, smokingStatusForCount(count = 2, limit = 3))
        assertEquals(SmokingStatus.WARNING, smokingStatusForCount(count = 3, limit = 3))
        assertEquals(SmokingStatus.DANGER, smokingStatusForCount(count = 4, limit = 3))
    }

    @Test
    fun smokingDashboardToneForCount_usesSimplifiedDashboardColors() {
        assertEquals(SmokingDashboardTone.SUCCESS, smokingDashboardToneForCount(count = 0, dailyLimit = 5))
        assertEquals(SmokingDashboardTone.WARNING, smokingDashboardToneForCount(count = 1, dailyLimit = 5))
        assertEquals(SmokingDashboardTone.WARNING, smokingDashboardToneForCount(count = 4, dailyLimit = 5))
        assertEquals(SmokingDashboardTone.DANGER, smokingDashboardToneForCount(count = 5, dailyLimit = 5))
        assertEquals(SmokingDashboardTone.DANGER, smokingDashboardToneForCount(count = 6, dailyLimit = 5))
        assertEquals(SmokingDashboardTone.SUCCESS, smokingDashboardToneForCount(count = 0, dailyLimit = 0))
        assertEquals(SmokingDashboardTone.DANGER, smokingDashboardToneForCount(count = 1, dailyLimit = 0))
    }

    @Test
    fun caffeineMonthRing_marksOverLimitWithoutTargetMet() {
        val date = LocalDate.of(2026, 4, 10)
        val days = buildCaffeineMonthRingDays(
            anchorDate = date,
            entriesByDate = mapOf(
                date to listOf(
                    CaffeineEntry(
                        id = 1,
                        date = date,
                        time = LocalTime.of(18, 0),
                        drinkType = CaffeineDrinkType.FILTER_COFFEE,
                        size = CaffeineDrinkSize.LARGE,
                        estimatedMg = 450,
                        customName = null,
                        createdAt = LocalDateTime.of(2026, 4, 10, 18, 0),
                    ),
                ),
            ),
            dailyLimitMg = 300,
        )

        val overLimitDay = days.first { it.dayLabel == "10" && it.isInCurrentMonth }

        assertTrue(overLimitDay.isOverLimit)
        assertFalse(overLimitDay.isTargetMet)
    }

    @Test
    fun smokingMonthRing_marksOverLimitWithoutTargetMet() {
        val state = buildSmokingDetailUiState(
            selectedDate = LocalDate.of(2026, 4, 10),
            selectedPeriod = TrendsPeriod.MONTHLY,
            entries = listOf(
                SmokingEntry(
                    date = LocalDate.of(2026, 4, 10),
                    count = 4,
                ),
            ),
            limit = 3,
        )

        val overLimitDay = state.monthDays.first { it.dayLabel == "10" && it.isInCurrentMonth }

        assertTrue(overLimitDay.isOverLimit)
        assertFalse(overLimitDay.isTargetMet)
    }
}
