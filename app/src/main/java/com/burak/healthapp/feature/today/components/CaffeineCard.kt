package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.components.ThickMetricProgressBar
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.feature.today.CaffeineCardState

@Composable
internal fun CaffeineCard(
    state: CaffeineCardState,
    onAdd: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails)
            .testTag("caffeine_card"),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_caffeine),
            trailing = {
                CardHeaderActionButton(
                    label = stringResource(R.string.common_add),
                    modifier = Modifier.testTag("caffeine_add_button"),
                    onClick = onAdd,
                )
            },
        )
        Column(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            Text(
                text = stringResource(
                    R.string.caffeine_today_total_formatted_format,
                    formatWholeNumber(state.dailyTotalMg),
                    formatWholeNumber(state.limitMg),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            ThickMetricProgressBar(
                progress = state.progress,
                activeColor = HealthPrimary,
                testTag = "caffeine_progress_bar",
            )
            Text(
                text = stringResource(R.string.caffeine_last_time_format, state.lastCaffeineTimeLabel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.overDailyLimit || state.afterCutoff || state.withinSleepBuffer) {
                Text(
                    text = stringResource(R.string.caffeine_estimate_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
