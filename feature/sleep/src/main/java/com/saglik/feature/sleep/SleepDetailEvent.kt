package com.saglik.feature.sleep

import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepQuality

sealed interface SleepDetailEvent {
    data class PeriodSelected(val periodType: PeriodType) : SleepDetailEvent
    data object StartTimeClicked : SleepDetailEvent
    data object WakeTimeClicked : SleepDetailEvent
    data object TimePickerDismissed : SleepDetailEvent
    data class TimePickerConfirmed(val hour: Int, val minute: Int) : SleepDetailEvent
    data class QualitySelected(val quality: SleepQuality?) : SleepDetailEvent
    data class NoteChanged(val value: String) : SleepDetailEvent
    data object SaveSleepClicked : SleepDetailEvent
}
