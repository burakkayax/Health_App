package com.saglik.domain.usecase.water

import com.saglik.core.model.WaterSummary
import com.saglik.domain.repository.WaterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class ObserveWaterSummaryUseCase (
    private val repository: WaterRepository,
) {
    operator fun invoke(): Flow<WaterSummary> {
        return repository.observeWaterEntries().map { entries ->
            if (entries.isEmpty()) {
                return@map WaterSummary(
                    totalTodayMl = 0,
                    totalLast7DaysMl = 0,
                    latestEntry = null,
                    hasData = false
                )
            }

            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val sevenDaysAgo = today.minusDays(6)

            var todayTotal = 0
            var last7DaysTotal = 0

            entries.forEach { entry ->
                val entryDate = Instant.ofEpochMilli(entry.recordedAtMillis)
                    .atZone(zone)
                    .toLocalDate()

                if (entryDate == today) {
                    todayTotal += entry.amountMl
                }
                
                if (!entryDate.isBefore(sevenDaysAgo) && !entryDate.isAfter(today)) {
                    last7DaysTotal += entry.amountMl
                }
            }

            WaterSummary(
                totalTodayMl = todayTotal,
                totalLast7DaysMl = last7DaysTotal,
                latestEntry = entries.firstOrNull(),
                hasData = true
            )
        }
    }
}
