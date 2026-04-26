package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.CardFooterLinkRow
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.CircularProgressRing
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.feature.today.meal.MealDraftFoodState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthFat
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthProtein
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.core.ui.theme.HealthSleep
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthWater
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.burak.healthapp.feature.today.formatFloat
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

internal fun ExerciseType?.toExerciseIcon(): ImageVector {
    return when (this) {
        ExerciseType.WEIGHTS -> Icons.Rounded.FitnessCenter
        ExerciseType.RUN -> Icons.Rounded.DirectionsRun
        ExerciseType.WALK -> Icons.Rounded.DirectionsWalk
        ExerciseType.BIKE -> Icons.Rounded.DirectionsBike
        ExerciseType.YOGA -> Icons.Rounded.SelfImprovement
        null -> Icons.Rounded.FitnessCenter
    }
}
