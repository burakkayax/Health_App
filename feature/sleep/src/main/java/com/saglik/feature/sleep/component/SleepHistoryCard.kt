package com.saglik.feature.sleep.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.feature.sleep.SleepHistoryItemUi

@Composable
fun SleepHistoryCard(
    history: List<SleepHistoryItemUi>,
    modifier: Modifier = Modifier,
) {
    GlassHealthCard(modifier = modifier) {
        HealthCardHeader(
            title = "History",
            accentColor = HealthColors.Ink,
            icon = Icons.Rounded.History,
            showChevron = false,
        )
        if (history.isEmpty()) {
            Text(
                text = "Your saved sleep entries will appear here.",
                modifier = Modifier.padding(top = 18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                history.forEachIndexed { index, item ->
                    SleepHistoryRow(item = item)
                    if (index != history.lastIndex) {
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
private fun SleepHistoryRow(
    item: SleepHistoryItemUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.dateText,
                style = MaterialTheme.typography.bodyLarge,
                color = HealthColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.timeRangeText,
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.durationText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HealthColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.qualityText != null) {
                Text(
                    text = item.qualityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HealthColors.SleepPurple,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
