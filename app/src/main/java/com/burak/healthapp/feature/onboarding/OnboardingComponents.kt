package com.burak.healthapp.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
fun OnboardingHeader(step: OnboardingStep) {
    if (step == OnboardingStep.WELCOME || step == OnboardingStep.DONE) return
    
    // total is TRACKING_AREAS, BASIC_INFO, ACTIVITY_GOAL, SMART_GOALS, PREFERENCES (5)
    // ordinal: WELCOME=0, TRACKING_AREAS=1, BASIC_INFO=2, ACTIVITY_GOAL=3, SMART_GOALS=4, PREFERENCES=5, DONE=6
    val currentIdx = step.ordinal
    val totalIdx = OnboardingStep.entries.size - 2 
    val progress = currentIdx.toFloat() / totalIdx.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = HealthSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.onboarding_step_progress, currentIdx, totalIdx),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = HealthPrimary
            )
        }
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = HealthPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun OnboardingFooter(
    step: OnboardingStep,
    onAction: (OnboardingAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        if (step != OnboardingStep.WELCOME && step != OnboardingStep.DONE) {
            RoundedPillButton(
                label = stringResource(R.string.onboarding_back),
                modifier = Modifier.weight(1f).testTag("onboarding_back_button"),
                onClick = { onAction(OnboardingAction.BackClicked) },
            )
        }
        
        if (step == OnboardingStep.WELCOME) {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                RoundedPillButton(
                    label = stringResource(R.string.onboarding_start),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_next_button"),
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                    onClick = { onAction(OnboardingAction.NextClicked) },
                )
                RoundedPillButton(
                    label = stringResource(R.string.onboarding_use_defaults),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(OnboardingAction.SkipWithDefaults) },
                )
            }
        } else {
            val primaryLabel = if (step == OnboardingStep.DONE) stringResource(R.string.onboarding_go_today) else stringResource(R.string.onboarding_next)
            val testTag = if (step == OnboardingStep.DONE) "onboarding_finish_button" else "onboarding_next_button"
            RoundedPillButton(
                label = primaryLabel,
                modifier = Modifier.weight(1f).testTag(testTag),
                containerColor = HealthPrimary,
                contentColor = Color.White,
                onClick = { onAction(OnboardingAction.NextClicked) },
            )
        }
    }
}
