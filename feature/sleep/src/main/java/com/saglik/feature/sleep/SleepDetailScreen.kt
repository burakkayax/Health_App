package com.saglik.feature.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.feature.sleep.component.AddSleepCard
import com.saglik.feature.sleep.component.SleepHistoryCard
import com.saglik.feature.sleep.component.SleepTrendCard

@Composable
fun SleepDetailScreen(
    state: SleepDetailUiState,
    onEvent: (SleepDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.cardGap),
    ) {
        item {
            SleepTrendCard(
                state = state,
                onPeriodSelected = { onEvent(SleepDetailEvent.PeriodSelected(it)) },
            )
        }
        item {
            AddSleepCard(
                state = state.addSleep,
                onStartChanged = { onEvent(SleepDetailEvent.StartChanged(it)) },
                onEndChanged = { onEvent(SleepDetailEvent.EndChanged(it)) },
                onQualitySelected = { onEvent(SleepDetailEvent.QualitySelected(it)) },
                onNoteChanged = { onEvent(SleepDetailEvent.NoteChanged(it)) },
                onSaveClick = { onEvent(SleepDetailEvent.SaveSleepClicked) },
            )
        }
        item {
            SleepHistoryCard(history = state.historyItems)
        }
    }
}
