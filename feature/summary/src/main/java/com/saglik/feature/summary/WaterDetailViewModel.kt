package com.saglik.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.domain.usecase.water.AddWaterEntryUseCase
import com.saglik.domain.usecase.water.DeleteWaterEntryUseCase
import com.saglik.domain.usecase.water.ObserveWaterEntriesUseCase
import com.saglik.domain.usecase.water.ObserveWaterSummaryUseCase
import com.saglik.domain.water.AddWaterEntryInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WaterDetailViewModel @Inject constructor(
    private val addWaterEntryUseCase: AddWaterEntryUseCase,
    private val deleteWaterEntryUseCase: DeleteWaterEntryUseCase,
    observeWaterEntriesUseCase: ObserveWaterEntriesUseCase,
    observeWaterSummaryUseCase: ObserveWaterSummaryUseCase,
) : ViewModel() {

    private val amountInputFlow = MutableStateFlow("")
    private val noteInputFlow = MutableStateFlow("")
    private val isSavingFlow = MutableStateFlow(false)
    private val errorMessageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WaterDetailUiState> = combine(
        combine(
            observeWaterSummaryUseCase(),
            observeWaterEntriesUseCase(),
            amountInputFlow,
            ::Triple
        ),
        combine(
            noteInputFlow,
            isSavingFlow,
            errorMessageFlow,
            ::Triple
        )
    ) { (summary, entries, amount), (note, isSaving, error) ->
        WaterDetailUiState(
            todayTotalText = String.format(Locale.US, "%,d ml", summary.totalTodayMl),
            last7DaysText = String.format(Locale.US, "%,d ml", summary.totalLast7DaysMl),
            latestEntryText = summary.latestEntry?.let { "${it.amountMl} ml" } ?: "No entries",
            amountInput = amount,
            noteInput = note,
            entries = entries.map { entry ->
                WaterEntryUiState(
                    id = entry.id,
                    amountText = "${entry.amountMl} ml",
                    recordedAtText = formatTime(entry.recordedAtMillis),
                    noteText = entry.note,
                    sourceText = entry.source.name
                )
            },
            isSaving = isSaving,
            errorMessage = error,
            hasData = summary.hasData
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WaterDetailUiState(
            todayTotalText = "0 ml",
            last7DaysText = "0 ml",
            latestEntryText = "No entries",
            amountInput = "",
            noteInput = "",
            entries = emptyList(),
            isSaving = false,
            errorMessage = null,
            hasData = false
        )
    )

    fun onAmountInputChanged(value: String) {
        amountInputFlow.value = value
        errorMessageFlow.value = null
    }

    fun onNoteInputChanged(value: String) {
        noteInputFlow.value = value
    }

    fun quickAddWater(amountMl: Int) {
        addWater(amountMl, null)
    }

    fun addWater() {
        val amount = amountInputFlow.value.toIntOrNull()
        if (amount == null) {
            errorMessageFlow.value = "Enter an amount greater than 0 ml."
            return
        }
        val note = noteInputFlow.value.takeIf { it.isNotBlank() }
        addWater(amount, note)
    }

    private fun addWater(amountMl: Int, note: String?) {
        viewModelScope.launch {
            isSavingFlow.value = true
            errorMessageFlow.value = null

            val input = AddWaterEntryInput(
                amountMl = amountMl,
                recordedAtMillis = System.currentTimeMillis(),
                note = note
            )

            val result = addWaterEntryUseCase(input)
            if (result.isSuccess) {
                amountInputFlow.value = ""
                noteInputFlow.value = ""
            } else {
                errorMessageFlow.value = result.exceptionOrNull()?.message ?: "Water entry could not be saved right now."
            }
            
            isSavingFlow.value = false
        }
    }

    fun deleteWaterEntry(id: String) {
        viewModelScope.launch {
            deleteWaterEntryUseCase(id)
        }
    }

    private fun formatTime(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.getDefault())
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter)
    }
}
