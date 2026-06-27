package com.saglik.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.DataSource
import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseSummary
import com.saglik.core.model.ExerciseType
import com.saglik.domain.usecase.ObserveExerciseSessionsUseCase
import com.saglik.domain.usecase.ObserveExerciseSummaryUseCase
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
class ExerciseDetailViewModel @Inject constructor(
    observeExerciseSummaryUseCase: ObserveExerciseSummaryUseCase,
    observeExerciseSessionsUseCase: ObserveExerciseSessionsUseCase,
) : ViewModel() {
    private val zone: ZoneId = ZoneId.systemDefault()

    val uiState: StateFlow<ExerciseDetailUiState> =
        combine(
            observeExerciseSummaryUseCase(),
            observeExerciseSessionsUseCase(),
        ) { summary, sessions ->
            summary.toUiState(sessions)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseDetailUiState.loading(),
        )

    private fun ExerciseSummary.toUiState(sessions: List<ExerciseSession>): ExerciseDetailUiState {
        val historyItems = sessions
            .sortedByDescending { it.endTimeMillis }
            .map { it.toHistoryItem() }

        if (sessionCount == 0 && historyItems.isEmpty()) {
            return ExerciseDetailUiState(
                sessionCountText = "No exercise yet",
                sessionLabelText = "Sync Health Connect or add a session later.",
                totalDurationText = "0 min total",
                latestSessionText = "No sessions logged",
                historyItems = emptyList(),
                isLoading = false,
                isEmpty = true,
            )
        }

        return ExerciseDetailUiState(
            sessionCountText = sessionCount.formatSessionCount(),
            sessionLabelText = if (sessionCount == 1) "Session" else "Sessions",
            totalDurationText = "${totalDurationMinutes.formatDuration()} total",
            latestSessionText = mostRecentSession?.displayTitle() ?: "Latest session unavailable",
            historyItems = historyItems,
            isLoading = false,
            isEmpty = false,
        )
    }

    private fun ExerciseSession.toHistoryItem(): ExerciseHistoryItemUiState {
        val start = startTimeMillis.toZonedDateTime()
        val end = endTimeMillis.toZonedDateTime()
        return ExerciseHistoryItemUiState(
            id = id,
            titleText = displayTitle(),
            dateText = end.toHistoryDateText(),
            timeRangeText = "${start.format(TimeFormatter)} - ${end.format(TimeFormatter)}",
            durationText = durationMinutes.formatDuration(),
            sourceText = sourceLabel(sourceAppName, source),
        )
    }

    private fun ExerciseSession.displayTitle(): String =
        title?.trim()?.takeIf { it.isNotEmpty() } ?: exerciseType.toDisplayText()

    private fun Int.formatSessionCount(): String =
        if (this == 1) "1 session" else "$this sessions"

    private fun Int.formatDuration(): String {
        val hours = this / 60
        val minutes = this % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
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

    private fun ExerciseType.toDisplayText(): String =
        when (this) {
            ExerciseType.WALKING -> "Walking"
            ExerciseType.RUNNING -> "Running"
            ExerciseType.CYCLING -> "Cycling"
            ExerciseType.SWIMMING -> "Swimming"
            ExerciseType.STRENGTH_TRAINING -> "Strength training"
            ExerciseType.YOGA -> "Yoga"
            ExerciseType.HIIT -> "HIIT"
            ExerciseType.OTHER -> "Other"
        }

    companion object {
        private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        private val DateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    }
}
