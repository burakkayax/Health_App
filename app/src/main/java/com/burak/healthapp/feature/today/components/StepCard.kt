package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.feature.today.StepCardState

@Composable
internal fun StepCard(
    state: StepCardState,
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
            progress = state.progress,
            color = HealthPrimary,
            headline = stringResource(
                R.string.today_steps_count_format,
                formatWholeNumber(state.currentSteps),
            ),
            supportingLabel = stringResource(
                R.string.today_steps_target_format,
                formatWholeNumber(state.targetSteps),
            ),
            helperLabel = stringResource(
                R.string.today_steps_week_format,
                formatWholeNumber(state.weeklySteps),
            ),
            trackColor = HealthPrimary.copy(alpha = 0.14f),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                contentDescription = null,
                tint = HealthPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
