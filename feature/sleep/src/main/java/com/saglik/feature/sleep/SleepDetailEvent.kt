package com.saglik.feature.sleep

import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepQuality

sealed interface SleepDetailEvent {
    data class PeriodSelected(val periodType: PeriodType) : SleepDetailEvent
    data class StartChanged(val value: String) : SleepDetailEvent
    data class EndChanged(val value: String) : SleepDetailEvent
    data class QualitySelected(val quality: SleepQuality?) : SleepDetailEvent
    data class NoteChanged(val value: String) : SleepDetailEvent
    data object SaveSleepClicked : SleepDetailEvent
}
