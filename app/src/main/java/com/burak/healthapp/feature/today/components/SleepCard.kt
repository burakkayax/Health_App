package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthSleep
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.feature.today.TodayUiState
@Composable
internal fun SleepCard(
    state: TodayUiState,
    onEdit: () -> Unit,
    onOpenDetails: () -> Unit,
    onDeleteSleep: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sleep_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_sleep),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    if (state.sleep.progress > 0f) {
                        DeleteIconButton(
                            testTag = "sleep_delete_button",
                            contentDescription = stringResource(R.string.content_description_delete_sleep),
                            onClick = onDeleteSleep,
                        )
                    }
                    CardHeaderActionButton(
                        label = stringResource(R.string.common_add),
                        modifier = Modifier.testTag("sleep_add_button"),
                        onClick = onEdit,
                    )
                }
            },
        )
        CompactRingMetricLayout(
            progress = state.sleep.progress,
            color = HealthSleep,
            headline = state.sleep.durationLabel,
            supportingLabel = state.sleep.timeRangeLabel,
            helperLabel = stringResource(R.string.today_format_target_text, state.sleep.targetLabel),
            trackColor = HealthSleep.copy(alpha = 0.14f),
        ) {
            Text(
                text = if (state.sleep.progress > 0f) state.sleep.durationLabel else "--",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}
