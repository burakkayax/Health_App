package com.saglik.feature.summary

import androidx.compose.runtime.Immutable

@Immutable
data class ExerciseDetailUiState(
    val sessionCountText: String,
    val sessionLabelText: String,
    val totalDurationText: String,
    val latestSessionText: String,
    val historyItems: List<ExerciseHistoryItemUiState>,
    val isLoading: Boolean,
    val isEmpty: Boolean,
) {
    companion object {
        fun loading(): ExerciseDetailUiState =
            ExerciseDetailUiState(
                sessionCountText = "Loading",
                sessionLabelText = "Reading sessions",
                totalDurationText = "Duration unavailable",
                latestSessionText = "No sessions logged",
                historyItems = emptyList(),
                isLoading = true,
                isEmpty = false,
            )
    }
}

@Immutable
data class ExerciseHistoryItemUiState(
    val id: String,
    val titleText: String,
    val dateText: String,
    val timeRangeText: String,
    val durationText: String,
    val sourceText: String,
)
