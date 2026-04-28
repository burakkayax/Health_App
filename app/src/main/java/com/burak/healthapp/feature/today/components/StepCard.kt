package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.feature.today.TodayUiState
@Composable
internal fun StepCard(
    state: TodayUiState,
    onOpenDetails: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("steps_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(title = stringResource(R.string.today_title_steps))
        CompactRingMetricLayout(
            progress = state.steps.progress,
            color = HealthPrimary,
            headline = state.steps.headline,
            supportingLabel = state.steps.supportingLabel,
            helperLabel = state.steps.helperLabel,
            trackColor = HealthPrimary.copy(alpha = 0.14f),
        ) {
            Icon(
                imageVector = Icons.Rounded.DirectionsWalk,
                contentDescription = null,
                tint = HealthPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
