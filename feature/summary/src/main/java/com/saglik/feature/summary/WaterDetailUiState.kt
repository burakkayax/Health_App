package com.saglik.feature.summary

import androidx.compose.runtime.Immutable

@Immutable
data class WaterDetailUiState(
    val todayTotalText: String,
    val last7DaysText: String,
    val latestEntryText: String,
    val amountInput: String,
    val noteInput: String,
    val entries: List<WaterEntryUiState>,
    val isSaving: Boolean,
    val errorMessage: String?,
    val hasData: Boolean,
)

@Immutable
data class WaterEntryUiState(
    val id: String,
    val amountText: String,
    val recordedAtText: String,
    val noteText: String?,
    val sourceText: String,
)
