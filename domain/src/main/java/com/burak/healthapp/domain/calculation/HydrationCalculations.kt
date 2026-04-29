package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.HydrationEntry
import java.time.LocalDate

fun calculateHydrationTotal(entries: List<HydrationEntry>): Int = entries.sumOf { it.amountMl }

fun averageWaterMl(entries: List<HydrationEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .groupBy(HydrationEntry::date)
        .mapValues { (_, hydrationEntries) -> hydrationEntries.sumOf { it.amountMl } }
    return averageByLoggedDays(totalsByDay)
}
