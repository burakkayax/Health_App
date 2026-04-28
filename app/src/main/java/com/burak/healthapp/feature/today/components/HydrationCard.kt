package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthWater
import com.burak.healthapp.feature.today.TodayUiState

@Composable
internal fun HydrationCard(
    state: TodayUiState,
    onQuickAdd: (Int) -> Unit,
    onMore: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hydration_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_hydration),
            trailing = {
                CardHeaderActionButton(
                    label = stringResource(R.string.common_add),
                    modifier = Modifier.testTag("hydration_add_button"),
                    onClick = onMore,
                )
            },
        )
        Column(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                Text(
                    text = stringResource(R.string.today_format_ml, state.hydration.currentMl),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.today_format_target_ml, state.hydration.targetMl),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(999.dp),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.hydration.progress.coerceIn(0f, 1f))
                        .height(12.dp)
                        .background(
                            color = HealthWater,
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                RoundedPillButton(
                    label = stringResource(R.string.today_format_quick_add_ml, 200),
                    onClick = { onQuickAdd(200) },
                )
                RoundedPillButton(
                    label = stringResource(R.string.today_format_quick_add_ml, 500),
                    onClick = { onQuickAdd(500) },
                )
            }
        }
    }
}
