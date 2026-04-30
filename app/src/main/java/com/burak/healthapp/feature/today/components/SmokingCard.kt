package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.TodayUiState
@Composable
internal fun SmokingCard(
    state: TodayUiState,
    onAddSmoking: () -> Unit,
    onQuickIncrement: () -> Unit,
    onDeleteSmoking: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val ringColor = when (state.smoking.status) {
        SmokingStatus.PASSIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        SmokingStatus.SAFE -> HealthSuccess
        SmokingStatus.WARNING -> HealthCarbs
        SmokingStatus.DANGER -> MaterialTheme.colorScheme.error
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
                    if (state.smoking.count > 0) {
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
        CompactRingMetricLayout(
            progress = state.smoking.progress,
            color = ringColor,
            headline = state.smoking.headline,
            supportingLabel = state.smoking.supportingLabel,
            helperLabel = state.smoking.helperLabel,
            trackColor = ringColor.copy(alpha = 0.14f),
            bottomContent = {
                RoundedPillButton(
                    label = "+1",
                    modifier = Modifier.testTag("smoking_quick_add_button"),
                    containerColor = ringColor.copy(alpha = 0.14f),
                    contentColor = ringColor,
                    onClick = onQuickIncrement,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = ringColor,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
