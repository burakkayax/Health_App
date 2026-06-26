@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.feature.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepEntry
import com.saglik.core.model.SleepQuality
import com.saglik.core.common.time.SleepTimeTextFormatter
import com.saglik.domain.sleep.SleepDetail
import com.saglik.domain.usecase.AddSleepEntryUseCase
import com.saglik.domain.usecase.ObserveSleepDetailUseCase
import com.saglik.domain.usecase.SleepInput
import com.saglik.domain.usecase.ValidateSleepInputUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SleepDetailViewModel @Inject constructor(
    private val observeSleepDetailUseCase: ObserveSleepDetailUseCase,
    private val validateSleepInputUseCase: ValidateSleepInputUseCase,
    private val addSleepEntryUseCase: AddSleepEntryUseCase,
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(PeriodType.WEEKLY)
    private val formState = MutableStateFlow(initialAddSleepState())
    private val zone: ZoneId = ZoneId.systemDefault()

    val uiState: StateFlow<SleepDetailUiState> =
        combine(
            selectedPeriod.flatMapLatest { observeSleepDetailUseCase(it) },
            formState,
        ) { detail, form ->
            detail.toUiState(form)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SleepDetailUiState.loading().copy(addSleep = formState.value),
        )

    fun onEvent(event: SleepDetailEvent) {
        when (event) {
            is SleepDetailEvent.PeriodSelected -> selectedPeriod.value = event.periodType
            is SleepDetailEvent.StartTimeClicked -> formState.update {
                it.copy(selectedPickerTarget = SleepTimeTarget.START, errorMessage = null)
            }
            is SleepDetailEvent.WakeTimeClicked -> formState.update {
                it.copy(selectedPickerTarget = SleepTimeTarget.WAKE, errorMessage = null)
            }
            is SleepDetailEvent.TimePickerDismissed -> formState.update {
                it.copy(selectedPickerTarget = null)
            }
            is SleepDetailEvent.TimePickerConfirmed -> formState.update {
                when (it.selectedPickerTarget) {
                    SleepTimeTarget.START -> it.copy(
                        startHour = event.hour,
                        startMinute = event.minute,
                        selectedPickerTarget = null,
                        errorMessage = null,
                    )
                    SleepTimeTarget.WAKE -> it.copy(
                        wakeHour = event.hour,
                        wakeMinute = event.minute,
                        selectedPickerTarget = null,
                        errorMessage = null,
                    )
                    null -> it
                }
            }
            is SleepDetailEvent.QualitySelected -> formState.update {
                it.copy(
                    selectedQuality = if (it.selectedQuality == event.quality) null else event.quality,
                    errorMessage = null,
                )
            }
            is SleepDetailEvent.NoteChanged -> formState.update {
                it.copy(note = event.value.take(MAX_NOTE_LENGTH), errorMessage = null)
            }
            SleepDetailEvent.SaveSleepClicked -> saveSleep()
        }
    }

    private fun saveSleep() {
        val input = formState.value.toSleepInput()
        val validation = validateSleepInputUseCase(input)
        if (!validation.isValid) {
            formState.update { it.copy(errorMessage = validation.errorMessage) }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }
            val saved = runCatching { addSleepEntryUseCase(input) }.getOrDefault(false)
            formState.update {
                if (saved) {
                    initialAddSleepState()
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Sleep could not be saved. Check the times and try again.",
                    )
                }
            }
        }
    }

    private fun SleepDetail.toUiState(form: AddSleepUiState): SleepDetailUiState =
        SleepDetailUiState(
            selectedPeriod = periodType,
            latestDurationText = latestDurationMinutes?.formatDuration() ?: "No sleep yet",
            averageText = averageMinutes?.formatDuration(),
            shortestText = shortestMinutes?.formatDuration(),
            longestText = longestMinutes?.formatDuration(),
            chartPoints = chartPoints,
            historyItems = entries.map { it.toHistoryItem() },
            isLoading = false,
            errorMessage = null,
            addSleep = form,
        )

    private fun AddSleepUiState.toSleepInput(): SleepInput {
        val startTime = LocalTime.of(startHour, startMinute)
        val endTime = LocalTime.of(wakeHour, wakeMinute)
        
        val endInstant = endTime.resolveEndInstant()
        val startInstant = startTime.resolveStartInstant(endTime)

        return SleepInput(
            startTime = startInstant,
            endTime = endInstant,
            quality = selectedQuality,
            note = note,
        )
    }

    private fun LocalTime.resolveEndInstant(): Instant {
        val now = ZonedDateTime.now(zone)
        val endDate = if (this.isAfter(now.toLocalTime())) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        return ZonedDateTime.of(endDate, this, zone).toKotlinInstant()
    }

    private fun LocalTime.resolveStartInstant(endTime: LocalTime): Instant {
        val now = ZonedDateTime.now(zone)
        val endDate = if (endTime.isAfter(now.toLocalTime())) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        val startDate = if (this.isAfter(endTime)) {
            endDate.minusDays(1)
        } else {
            endDate
        }
        return ZonedDateTime.of(startDate, this, zone).toKotlinInstant()
    }

    private fun SleepEntry.toHistoryItem(): SleepHistoryItemUi {
        val start = startTime.toJavaZonedDateTime()
        val end = endTime.toJavaZonedDateTime()
        val endDate = end.toLocalDate()
        val today = LocalDate.now(zone)
        val dateText = when (endDate) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> endDate.format(HistoryDateFormatter)
        }

        return SleepHistoryItemUi(
            id = id,
            dateText = dateText,
            timeRangeText = "${start.format(TimeOutputFormatter)} - ${end.format(TimeOutputFormatter)}",
            durationText = durationMinutes.formatDuration(),
            qualityText = quality.toQualityLabel(),
        )
    }

    private fun Int.formatDuration(): String {
        val hours = this / 60
        val minutes = this % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun SleepQuality?.toQualityLabel(): String? =
        when (this) {
            SleepQuality.POOR -> "Poor"
            SleepQuality.OKAY -> "Okay"
            SleepQuality.GOOD -> "Good"
            SleepQuality.EXCELLENT -> "Excellent"
            null -> null
        }

    private fun Instant.toJavaZonedDateTime(): ZonedDateTime =
        java.time.Instant.ofEpochMilli(toEpochMilliseconds()).atZone(zone)

    private fun ZonedDateTime.toKotlinInstant(): Instant =
        Instant.fromEpochMilliseconds(toInstant().toEpochMilli())

    private fun initialAddSleepState(): AddSleepUiState {
        val end = ZonedDateTime.now().minusMinutes(15)
        val start = end.minusHours(8)
        return AddSleepUiState(
            startHour = start.hour,
            startMinute = start.minute,
            wakeHour = end.hour,
            wakeMinute = end.minute,
        )
    }

    companion object {
        private const val MAX_NOTE_LENGTH = 160
        private val TimeOutputFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        private val HistoryDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    }
}
