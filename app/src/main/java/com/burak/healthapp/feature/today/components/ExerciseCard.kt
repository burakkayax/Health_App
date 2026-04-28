package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.feature.today.TodayUiState
@Composable
internal fun ExerciseCard(
    state: TodayUiState,
    onAddExercise: () -> Unit,
    onDeleteExercise: () -> Unit,
) {
    val ringColor = if (state.exercise.type == null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        HealthPrimary
    }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_card"),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_exercise),
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    if (state.exercise.type != null) {
                        DeleteIconButton(
                            testTag = "exercise_delete_button",
                            contentDescription = stringResource(R.string.content_description_delete_exercise),
                            onClick = onDeleteExercise,
                        )
                    }
                    CardHeaderActionButton(
                        label = stringResource(R.string.common_add),
                        modifier = Modifier.testTag("exercise_add_button"),
                        onClick = onAddExercise,
                    )
                }
            },
        )
        CompactRingMetricLayout(
            progress = state.exercise.progress,
            color = ringColor,
            headline = state.exercise.title,
            supportingLabel = state.exercise.durationLabel,
            helperLabel = "${state.exercise.intensityLabel} • ${state.exercise.helperLabel}",
        ) {
            Icon(
                imageVector = state.exercise.type.toExerciseIcon(),
                contentDescription = null,
                tint = ringColor,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

internal fun ExerciseType?.toExerciseIcon(): ImageVector = when (this) {
    ExerciseType.WEIGHTS -> Icons.Rounded.FitnessCenter
    ExerciseType.RUN -> Icons.Rounded.DirectionsRun
    ExerciseType.WALK -> Icons.Rounded.DirectionsWalk
    ExerciseType.BIKE -> Icons.Rounded.DirectionsBike
    ExerciseType.YOGA -> Icons.Rounded.SelfImprovement
    null -> Icons.Rounded.FitnessCenter
}
