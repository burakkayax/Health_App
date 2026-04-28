package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.CircularProgressRing
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.formatFloat
@Composable
internal fun SupplementsCard(
    items: List<SupplementItemState>,
    onAdd: () -> Unit,
    onDeleteDose: (Long) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplements_card"),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_supplements),
            trailing = {
                CardHeaderActionButton(
                    label = stringResource(R.string.common_add),
                    modifier = Modifier.testTag("supplements_add_button"),
                    onClick = onAdd,
                )
            },
        )
        if (items.isEmpty()) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                text = stringResource(R.string.today_empty_supplements),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HealthSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                items(items, key = { it.id }) { item ->
                    SupplementRingItem(
                        item = item,
                        onDeleteDose = onDeleteDose,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplementRingItem(
    item: SupplementItemState,
    onDeleteDose: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.width(112.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        CircularProgressRing(
            progress = item.progress,
            color = HealthPrimary,
            modifier = Modifier.size(88.dp),
            strokeWidth = 7.dp,
            trackColor = HealthPrimary.copy(alpha = 0.14f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = formatFloat(item.currentAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.unitLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(
                R.string.today_format_supplement_amount,
                formatFloat(item.currentAmount),
                formatFloat(item.targetAmount),
                item.unitLabel,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.currentAmount > 0f) {
            DeleteIconButton(
                testTag = "supplement_dose_delete_${item.id}",
                contentDescription = stringResource(R.string.content_description_delete_supplement_dose),
                onClick = { onDeleteDose(item.id) },
            )
        }
    }
}
