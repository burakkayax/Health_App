package com.burak.healthapp.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("onboarding_root"),
        contentAlignment = Alignment.TopCenter,
    ) {
        val maxWidth = if (maxWidth > 600.dp) 600.dp else maxWidth

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = HealthSpacing.sm, vertical = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                OnboardingHeader(step = state.currentStep)
            }

            item {
                state.saveError?.let { error ->
                    Text(
                        text = error.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = HealthSpacing.sm),
                    )
                }
            }

            item {
                when (state.currentStep) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.TRACKING_AREAS -> TrackingAreasStep(state, onAction)
                    OnboardingStep.BASIC_INFO -> BasicInfoStep(state, onAction)
                    OnboardingStep.ACTIVITY_GOAL -> ActivityGoalStep(state, onAction)
                    OnboardingStep.SMART_GOALS -> SmartGoalsStep(state, onAction)
                    OnboardingStep.PREFERENCES -> PreferencesStep(state, onAction)
                    OnboardingStep.DONE -> DoneStep()
                }
            }

            if (state.isSaving) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = HealthSpacing.xs),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = HealthPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.size(HealthSpacing.xs))
                        Text(text = stringResource(R.string.common_saving), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f))
                    }
                }
            } else {
                item {
                    OnboardingFooter(
                        step = state.currentStep,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}
