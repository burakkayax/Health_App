package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.format.formatMetricCount
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.feature.today.SmokingCardState

internal enum class SmokingDashboardTone {
    SUCCESS,
    WARNING,
    DANGER,
}

internal fun smokingDashboardToneForCount(
    count: Int,
    dailyLimit: Int,
): SmokingDashboardTone = when {
    count <= 0 -> SmokingDashboardTone.SUCCESS
    dailyLimit <= 0 -> SmokingDashboardTone.DANGER
    count >= dailyLimit -> SmokingDashboardTone.DANGER
    else -> SmokingDashboardTone.WARNING
}

@Composable
internal fun SmokingCard(
    state: SmokingCardState,
    onAddSmoking: () -> Unit,
    onQuickIncrement: () -> Unit,
    onDeleteSmoking: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val statusColor = when (smokingDashboardToneForCount(state.count, state.limit)) {
        SmokingDashboardTone.SUCCESS -> HealthSuccess
        SmokingDashboardTone.WARNING -> HealthCarbs
        SmokingDashboardTone.DANGER -> MaterialTheme.colorScheme.error
    }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails)
            .testTag("smoking_card"),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_smoking),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    if (state.count > 0) {
                        DeleteIconButton(
                            testTag = "smoking_delete_button",
                            contentDescription = stringResource(R.string.content_description_delete_smoking),
                            onClick = onDeleteSmoking,
                        )
                    }
                    CardHeaderActionButton(
                        label = stringResource(R.string.common_add),
                        modifier = Modifier.testTag("smoking_add_button"),
                        onClick = onAddSmoking,
                    )
                }
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmokingStatusCircle(
                count = state.count,
                containerColor = statusColor.copy(alpha = 0.16f),
                contentColor = statusColor,
                contentDescription = stringResource(
                    R.string.smoking_status_circle_description,
                    formatMetricCount(state.count),
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                Text(
                    text = state.supportingLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = state.helperLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RoundedPillButton(
                    label = "+1",
                    modifier = Modifier.testTag("smoking_quick_add_button"),
                    containerColor = statusColor.copy(alpha = 0.14f),
                    contentColor = statusColor,
                    onClick = onQuickIncrement,
                )
            }
        }
    }
}

@Composable
private fun SmokingStatusCircle(
    count: Int,
    containerColor: Color,
    contentColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(containerColor)
            .semantics { this.contentDescription = contentDescription }
            .testTag("smoking_status_circle"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.testTag("smoking_status_count"),
                text = formatMetricCount(count),
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.smoking_status_circle_unit),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
