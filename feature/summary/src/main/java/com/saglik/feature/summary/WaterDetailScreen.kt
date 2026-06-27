package com.saglik.feature.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing

@Composable
fun WaterDetailScreen(
    state: WaterDetailUiState,
    onAmountInputChanged: (String) -> Unit,
    onNoteInputChanged: (String) -> Unit,
    onAddWaterClick: () -> Unit,
    onQuickAddClick: (Int) -> Unit,
    onDeleteEntryClick: (String) -> Unit,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.cardGap),
    ) {
        item {
            WaterTrendHeroCard(state = state)
        }
        item {
            QuickAddCard(onQuickAddClick = onQuickAddClick)
        }
        item {
            AddWaterCard(
                state = state,
                onAmountInputChanged = onAmountInputChanged,
                onNoteInputChanged = onNoteInputChanged,
                onAddWaterClick = onAddWaterClick,
            )
        }
        item {
            WaterHistoryCard(
                entries = state.entries,
                onDeleteEntryClick = onDeleteEntryClick,
            )
        }
    }
}

@Composable
private fun WaterTrendHeroCard(
    state: WaterDetailUiState,
    modifier: Modifier = Modifier,
) {
    com.saglik.core.ui.component.card.HealthDetailHeroCard(
        title = "Water Log (Today)",
        mainValue = state.todayTotalText,
        secondaryText = "Latest: ${state.latestEntryText}",
        modifier = modifier,
        contentSlot = {
            TrendStatRow(
                label = "Last 7 days",
                value = state.last7DaysText,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    )
}

@Composable
private fun TrendStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickAddCard(
    onQuickAddClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    com.saglik.core.ui.component.GlassHealthCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.HealthCardHeader(
                title = "Quick Add",
                accentColor = HealthColors.SystemBlue,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                com.saglik.core.ui.component.form.HealthSecondaryButton(
                    text = "+250 ml",
                    onClick = { onQuickAddClick(250) },
                    modifier = Modifier.weight(1f)
                )
                com.saglik.core.ui.component.form.HealthSecondaryButton(
                    text = "+500 ml",
                    onClick = { onQuickAddClick(500) },
                    modifier = Modifier.weight(1f)
                )
                com.saglik.core.ui.component.form.HealthSecondaryButton(
                    text = "+750 ml",
                    onClick = { onQuickAddClick(750) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AddWaterCard(
    state: WaterDetailUiState,
    onAmountInputChanged: (String) -> Unit,
    onNoteInputChanged: (String) -> Unit,
    onAddWaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.saglik.core.ui.component.card.HealthAddEntryCard(
        title = "Add Custom Amount",
        modifier = modifier
    ) {
        com.saglik.core.ui.component.form.HealthNumberInput(
            value = state.amountInput,
            onValueChange = onAmountInputChanged,
            label = "Amount",
            suffix = "ml",
            isError = state.errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        com.saglik.core.ui.component.form.HealthTextInput(
            value = state.noteInput,
            onValueChange = onNoteInputChanged,
            label = "Note (optional)",
            modifier = Modifier.fillMaxWidth()
        )
        if (state.errorMessage != null) {
            com.saglik.core.ui.component.form.HealthValidationMessage(
                message = state.errorMessage,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        com.saglik.core.ui.component.form.HealthPrimaryButton(
            text = if (state.isSaving) "Saving" else "Add Water",
            onClick = onAddWaterClick,
            enabled = !state.isSaving && state.amountInput.isNotBlank(),
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Composable
private fun WaterHistoryCard(
    entries: List<WaterEntryUiState>,
    onDeleteEntryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    com.saglik.core.ui.component.card.HealthHistoryCard(
        title = "History",
        modifier = modifier
    ) {
        if (entries.isEmpty()) {
            com.saglik.core.ui.component.state.HealthEmptyState(
                message = "Your saved water entries will appear here.",
                modifier = Modifier.padding(top = 18.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                entries.forEachIndexed { index, item ->
                    WaterHistoryRow(
                        item = item,
                        onDeleteClick = { onDeleteEntryClick(item.id) }
                    )
                    if (index != entries.lastIndex) {
                        HorizontalDivider(
                            color = HealthColors.GlassBorder.copy(alpha = 0.58f),
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterHistoryRow(
    item: WaterEntryUiState,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HealthColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.recordedAtText,
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
            if (item.noteText != null) {
                Text(
                    text = item.noteText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthColors.SecondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete entry",
                tint = HealthColors.SecondaryText
            )
        }
    }
}
