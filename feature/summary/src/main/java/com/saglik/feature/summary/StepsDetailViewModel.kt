package com.saglik.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.DataSource
import com.saglik.core.model.StepsEntry
import com.saglik.domain.steps.StepsSummary
import com.saglik.domain.usecase.ObserveStepsEntriesUseCase
import com.saglik.domain.usecase.ObserveStepsSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class StepsDetailViewModel @Inject constructor(
    observeStepsSummaryUseCase: ObserveStepsSummaryUseCase,
    observeStepsEntriesUseCase: ObserveStepsEntriesUseCase,
) : ViewModel() {
    private val zone: ZoneId = ZoneId.systemDefault()

    val uiState: StateFlow<StepsDetailUiState> =
        combine(
            observeStepsSummaryUseCase(),
            observeStepsEntriesUseCase(),
        ) { summary, entries ->
            summary.toUiState(entries)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StepsDetailUiState.loading(),
        )

    private fun StepsSummary.toUiState(entries: List<StepsEntry>): StepsDetailUiState {
        val historyItems = entries
            .sortedByDescending { it.endTimeMillis }
            .map { it.toHistoryItem() }

        if (!hasData && historyItems.isEmpty()) {
            return StepsDetailUiState(
                todayStepsText = "No steps yet",
                todayLabelText = "Sync Health Connect to import steps.",
                lastSevenDaysText = "Last 7 days unavailable",
                latestEntryText = "No entries logged",
                historyItems = emptyList(),
                isLoading = false,
                isEmpty = true,
            )
        }

        return StepsDetailUiState(
            todayStepsText = String.format(Locale.US, "%,d steps", totalStepsToday),
            todayLabelText = "Today",
            lastSevenDaysText = String.format(Locale.US, "%,d in last 7 days", totalStepsLast7Days),
            latestEntryText = latestEntryCount?.let { String.format(Locale.US, "%,d steps", it) }
                ?: "Latest entry unavailable",
            historyItems = historyItems,
            isLoading = false,
            isEmpty = false,
        )
    }

    private fun StepsEntry.toHistoryItem(): StepsHistoryItemUiState {
        val start = startTimeMillis.toZonedDateTime()
        val end = endTimeMillis.toZonedDateTime()
        return StepsHistoryItemUiState(
            id = id,
            dateText = end.toHistoryDateText(),
            timeRangeText = "${start.format(TimeFormatter)} - ${end.format(TimeFormatter)}",
            countText = String.format(Locale.US, "%,d steps", count),
            sourceText = sourceLabel(sourceAppName, source),
        )
    }

    private fun Long.toZonedDateTime(): ZonedDateTime =
        Instant.ofEpochMilli(this).atZone(zone)

    private fun ZonedDateTime.toHistoryDateText(): String {
        val date = toLocalDate()
        val today = LocalDate.now(zone)
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateFormatter)
        }
    }

    private fun sourceLabel(sourceAppName: String?, source: DataSource): String =
        sourceAppName?.trim()?.takeIf { it.isNotEmpty() } ?: source.toDisplayText()

    private fun DataSource.toDisplayText(): String =
        when (this) {
            DataSource.MANUAL -> "Manual"
            DataSource.HEALTH_CONNECT -> "Health Connect"
            DataSource.IMPORTED -> "Imported"
            DataSource.ESTIMATED -> "Estimated"
        }

    companion object {
        private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        private val DateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    }
}
