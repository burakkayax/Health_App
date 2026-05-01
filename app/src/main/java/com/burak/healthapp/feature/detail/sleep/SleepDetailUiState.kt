package com.burak.healthapp.feature.detail.sleep

import com.burak.healthapp.domain.model.TrendsPeriod

data class SleepBarState(
    val label: String,
    val progress: Float,
    val durationLabel: String,
)

data class SleepCalendarDayState(
    val dayLabel: String,
    val progress: Float,
    val hasData: Boolean,
    val isInCurrentMonth: Boolean,
    val isTargetMet: Boolean,
    val dateLabel: String = dayLabel,
    val durationLabel: String = "",
    val isToday: Boolean = false,
)

data class SleepCalendarWeekState(
    val days: List<SleepCalendarDayState>,
)

data class SleepRegularityState(
    val title: String,
    val subtitle: String,
    val helperLabel: String,
    val progress: Float?,
    val status: SleepRegularityStatus,
    val isEmpty: Boolean = false,
)

enum class SleepRegularityStatus {
    EMPTY,
    REGULAR,
    VARIABLE,
    IRREGULAR,
}

data class SleepDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val isLoading: Boolean = false,
    val bars: List<SleepBarState>,
    val regularity: SleepRegularityState,
    val hasData: Boolean,
    val targetLabel: String,
    val bedtimeLabel: String,
    val wakeLabel: String,
    val calendarWeeks: List<SleepCalendarWeekState> = emptyList(),
)
