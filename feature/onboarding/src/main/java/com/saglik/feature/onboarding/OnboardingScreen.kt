package com.saglik.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex
import com.saglik.core.ui.component.GlassHealthCard

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.currentStep != OnboardingStep.WELCOME) {
        onEvent(OnboardingEvent.BackClicked)
    }

    OnboardingScaffold(
        currentStep = state.currentStep,
        showBackButton = state.currentStep != OnboardingStep.WELCOME,
        onBackClick = { onEvent(OnboardingEvent.BackClicked) },
        footer = {
            OnboardingError(message = state.errorMessage)
            OnboardingPrimaryButton(
                text = state.currentStep.primaryButtonText(),
                enabled = state.isNextEnabled,
                isLoading = state.isSaving,
                onClick = {
                    if (state.currentStep == OnboardingStep.REVIEW) {
                        onEvent(OnboardingEvent.CompleteClicked)
                    } else {
                        onEvent(OnboardingEvent.NextClicked)
                    }
                },
            )
        },
    ) {
        when (state.currentStep) {
            OnboardingStep.WELCOME -> WelcomeStep()
            OnboardingStep.SEX -> SexStep(
                selectedSex = state.sex,
                onSexSelected = { onEvent(OnboardingEvent.SexSelected(it)) },
            )
            OnboardingStep.AGE -> AgeStep(
                value = state.age,
                onValueChange = { onEvent(OnboardingEvent.AgeChanged(it)) },
            )
            OnboardingStep.HEIGHT -> HeightStep(
                value = state.heightCm,
                onValueChange = { onEvent(OnboardingEvent.HeightChanged(it)) },
            )
            OnboardingStep.WEIGHT -> StartingWeightStep(
                value = state.startingWeightKg,
                onValueChange = { onEvent(OnboardingEvent.StartingWeightChanged(it)) },
            )
            OnboardingStep.GOAL -> GoalStep(
                selectedGoal = state.goal,
                onGoalSelected = { onEvent(OnboardingEvent.GoalSelected(it)) },
            )
            OnboardingStep.REVIEW -> ReviewStep(state = state)
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.contentGap),
    ) {
        OnboardingStepHeader(
            title = "Track your health.",
            subtitle = "Understand your trends.\nBuild better habits.",
            large = true,
        )
        GlassHealthCard(modifier = Modifier.padding(top = 14.dp)) {
            Text(
                text = "Start with the basics, then keep your daily health picture close.",
                style = MaterialTheme.typography.titleLarge,
                color = HealthColors.Ink,
            )
            Text(
                text = "Your profile stays on this device and helps shape future tracking screens.",
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = HealthColors.SecondaryText,
            )
        }
    }
}

@Composable
private fun SexStep(
    selectedSex: Sex?,
    onSexSelected: (Sex) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OnboardingStepHeader(
            title = "A little about you",
            subtitle = "Choose the option that best fits.",
        )
        Sex.entries.forEach { sex ->
            OnboardingSelectionCard(
                title = sex.displayName(),
                selected = selectedSex == sex,
                onClick = { onSexSelected(sex) },
            )
        }
    }
}

@Composable
private fun AgeStep(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        OnboardingStepHeader(
            title = "How old are you?",
            subtitle = "Use your age for now. Birth date can come later.",
        )
        OnboardingNumberInput(
            value = value,
            onValueChange = onValueChange,
            unit = "years",
            placeholder = "32",
            keyboardType = KeyboardType.Number,
        )
    }
}

@Composable
private fun HeightStep(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        OnboardingStepHeader(
            title = "Your height",
            subtitle = "Metric only for this first setup.",
        )
        OnboardingNumberInput(
            value = value,
            onValueChange = onValueChange,
            unit = "cm",
            placeholder = "175",
        )
    }
}

@Composable
private fun StartingWeightStep(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        OnboardingStepHeader(
            title = "Starting weight",
            subtitle = "This becomes your first manual weight entry.",
        )
        OnboardingNumberInput(
            value = value,
            onValueChange = onValueChange,
            unit = "kg",
            placeholder = "72",
        )
    }
}

@Composable
private fun GoalStep(
    selectedGoal: HealthGoal?,
    onGoalSelected: (HealthGoal) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        OnboardingStepHeader(
            title = "What is your goal?",
            subtitle = "Pick the direction that feels most useful right now.",
        )
        HealthGoal.entries.forEach { goal ->
            OnboardingSelectionCard(
                title = goal.displayName(),
                selected = selectedGoal == goal,
                onClick = { onGoalSelected(goal) },
            )
        }
    }
}

@Composable
private fun ReviewStep(
    state: OnboardingUiState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        OnboardingStepHeader(
            title = "Review your profile",
            subtitle = "Make sure everything looks right before you start.",
        )
        GlassHealthCard {
            ReviewRow(label = "Sex", value = state.sex?.displayName().orEmpty())
            ReviewRow(label = "Age", value = state.age.withUnit("years"))
            ReviewRow(label = "Height", value = state.heightCm.withUnit("cm"))
            ReviewRow(label = "Starting weight", value = state.startingWeightKg.withUnit("kg"))
            ReviewRow(label = "Goal", value = state.goal?.displayName().orEmpty())
        }
    }
}

@Composable
private fun ReviewRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = HealthColors.SecondaryText,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = HealthColors.Ink,
            textAlign = TextAlign.End,
        )
    }
}

private fun OnboardingStep.primaryButtonText(): String =
    when (this) {
        OnboardingStep.WELCOME -> "Get Started"
        OnboardingStep.REVIEW -> "Start Tracking"
        else -> "Continue"
    }

private fun Sex.displayName(): String =
    when (this) {
        Sex.MALE -> "Male"
        Sex.FEMALE -> "Female"
        Sex.OTHER -> "Other"
        Sex.UNSPECIFIED -> "Prefer not to say"
    }

private fun HealthGoal.displayName(): String =
    when (this) {
        HealthGoal.LOSE_WEIGHT -> "Lose weight"
        HealthGoal.GAIN_WEIGHT -> "Gain weight"
        HealthGoal.MAINTAIN_WEIGHT -> "Maintain weight"
        HealthGoal.BUILD_MUSCLE -> "Build muscle"
        HealthGoal.GENERAL_HEALTH -> "General health"
    }

private fun String.withUnit(unit: String): String =
    if (isBlank()) "" else "$this $unit"
