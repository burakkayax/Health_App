package com.saglik.feature.summary

import androidx.compose.runtime.Immutable

@Immutable
data class StepsDetailUiState(
    val todayStepsText: String,
    val todayLabelText: String,
    val lastSevenDaysText: String,
    val latestEntryText: String,
    val historyItems: List<StepsHistoryItemUiState>,
    val isLoading: Boolean,
    val isEmpty: Boolean,
) {
    companion object {
        fun loading(): StepsDetailUiState =
            StepsDetailUiState(
                todayStepsText = "Loading",
                todayLabelText = "Reading steps",
                lastSevenDaysText = "Last 7 days unavailable",
                latestEntryText = "Latest entry unavailable",
                historyItems = emptyList(),
                isLoading = true,
                isEmpty = false,
            )
    }
}

@Immutable
data class StepsHistoryItemUiState(
    val id: String,
    val dateText: String,
    val timeRangeText: String,
    val countText: String,
    val sourceText: String,
)
