package com.saglik.feature.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.ui.component.EditChip
import com.saglik.feature.summary.component.BmiSummaryCard
import com.saglik.feature.summary.component.ExerciseSummaryCard
import com.saglik.feature.summary.component.MoodOrInsightSummaryCard
import com.saglik.feature.summary.component.SleepSummaryCard
import com.saglik.feature.summary.component.StepsSummaryCard
import com.saglik.feature.summary.component.WeightSummaryCard

@Composable
fun SummaryScreen(
    state: SummaryUiState,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onEditPinnedClick: () -> Unit = {},
    onWeightClick: () -> Unit = {},
    onBmiClick: () -> Unit = {},
    onSleepClick: () -> Unit = {},
) {
    val cards = remember(state) {
        listOf("weight", "bmi", "sleep", "steps", "exercise", "mood")
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.cardGap),
    ) {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                EditChip(onClick = onEditPinnedClick)
            }
        }
        items(cards) { card ->
            when (card) {
                "weight" -> WeightSummaryCard(
                    summary = state.weight,
                    onClick = onWeightClick,
                )

                "bmi" -> BmiSummaryCard(
                    summary = state.bmi,
                    onClick = onBmiClick,
                )

                "sleep" -> SleepSummaryCard(
                    summary = state.sleep,
                    onClick = onSleepClick,
                )

                "steps" -> StepsSummaryCard(summary = state.steps)

                "exercise" -> ExerciseSummaryCard(summary = state.exercise)

                "mood" -> MoodOrInsightSummaryCard(summary = state.mood)
            }
        }
    }
}
