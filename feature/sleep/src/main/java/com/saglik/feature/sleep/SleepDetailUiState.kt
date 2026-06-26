package com.saglik.feature.sleep

import androidx.compose.runtime.Immutable
import com.saglik.core.model.ChartPoint
import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepQuality

@Immutable
data class SleepDetailUiState(
    val selectedPeriod: PeriodType,
    val latestDurationText: String,
    val averageText: String?,
    val shortestText: String?,
    val longestText: String?,
    val chartPoints: List<ChartPoint>,
    val historyItems: List<SleepHistoryItemUi>,
    val isLoading: Boolean,
    val errorMessage: String?,
    val addSleep: AddSleepUiState,
) {
    companion object {
        fun loading(): SleepDetailUiState =
            SleepDetailUiState(
                selectedPeriod = PeriodType.WEEKLY,
                latestDurationText = "No sleep yet",
                averageText = null,
                shortestText = null,
                longestText = null,
                chartPoints = emptyList(),
                historyItems = emptyList(),
                isLoading = true,
                errorMessage = null,
                addSleep = AddSleepUiState(),
            )
    }
}

@Immutable
data class AddSleepUiState(
    val startText: String = "",
    val endText: String = "",
    val selectedQuality: SleepQuality? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@Immutable
data class SleepHistoryItemUi(
    val id: String,
    val dateText: String,
    val timeRangeText: String,
    val durationText: String,
    val qualityText: String?,
)
