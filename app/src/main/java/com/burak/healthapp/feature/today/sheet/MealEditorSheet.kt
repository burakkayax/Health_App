package com.burak.healthapp.feature.today.sheet

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
import com.burak.healthapp.core.ui.text.asString
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.burak.healthapp.feature.today.formatFloat
import com.burak.healthapp.feature.today.toFloatOrDefault
import com.burak.healthapp.feature.today.toIntOrDefault
import com.burak.healthapp.feature.today.toLocalTimeOrNull
import com.burak.healthapp.feature.today.components.toExerciseIcon
@Composable
internal fun MealEditorSheet(
    state: MealEditorUiState,
    onMealTypeChange: (MealType) -> Unit,
    onAddDraft: () -> Unit,
    onRemoveDraft: (Long) -> Unit,
    onDraftNameChange: (Long, String) -> Unit,
    onDraftCaloriesChange: (Long, String) -> Unit,
    onDraftProteinChange: (Long, String) -> Unit,
    onDraftCarbsChange: (Long, String) -> Unit,
    onDraftFatChange: (Long, String) -> Unit,
    onSaveMeal: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        contentPadding = PaddingValues(bottom = HealthSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            Text(
                text = stringResource(R.string.today_sheet_meal_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                MealType.entries.forEach { mealType ->
                    FilterChip(
                        selected = state.mealType == mealType,
                        onClick = { onMealTypeChange(mealType) },
                        label = { Text(mealType.label) },
                    )
                }
            }
        }
        items(
            items = state.draftFoods,
            key = MealDraftFoodState::draftId,
        ) { draft ->
            HealthCard(modifier = Modifier.testTag("meal_draft_${draft.draftId}")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.today_meal_food),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.draftFoods.size > 1) {
                        RoundedPillButton(
                            label = stringResource(R.string.common_delete),
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.error,
                            onClick = { onRemoveDraft(draft.draftId) },
                        )
                    }
                }
                HealthPillTextField(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    value = draft.name,
                    onValueChange = { onDraftNameChange(draft.draftId, it) },
                    label = stringResource(R.string.today_meal_food_name),
                    isError = draft.nameError != null,
                    supportingText = draft.nameError?.asString(),
                )
                NumberFieldRow(
                    leftLabel = stringResource(R.string.today_meal_calories),
                    leftValue = draft.calories,
                    rightLabel = stringResource(R.string.today_meal_protein),
                    rightValue = draft.protein,
                    onLeftChange = { onDraftCaloriesChange(draft.draftId, it) },
                    onRightChange = { onDraftProteinChange(draft.draftId, it) },
                )
                NumberFieldRow(
                    leftLabel = stringResource(R.string.today_meal_carbs),
                    leftValue = draft.carbs,
                    rightLabel = stringResource(R.string.today_meal_fat),
                    rightValue = draft.fat,
                    onLeftChange = { onDraftCarbsChange(draft.draftId, it) },
                    onRightChange = { onDraftFatChange(draft.draftId, it) },
                )
                val fieldError = draft.calorieError ?: draft.macroError
                if (fieldError != null) {
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = fieldError.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        item {
            RoundedPillButton(
                label = stringResource(R.string.today_meal_add_food),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meal_sheet_add_food_button"),
                containerColor = HealthPrimary.copy(alpha = 0.12f),
                contentColor = HealthPrimary,
                onClick = onAddDraft,
            )
        }
        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meal_sheet_save_button"),
                enabled = state.canSave,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                ),
                onClick = onSaveMeal,
                contentPadding = PaddingValues(vertical = HealthSpacing.sm),
            ) {
                Text(text = stringResource(R.string.today_meal_save))
            }
        }
    }
}

@Composable
private fun MealDraftFields(
    draft: MealDraftFoodState,
    onDraftNameChange: (Long, String) -> Unit,
    onDraftCaloriesChange: (Long, String) -> Unit,
    onDraftProteinChange: (Long, String) -> Unit,
    onDraftCarbsChange: (Long, String) -> Unit,
    onDraftFatChange: (Long, String) -> Unit,
) {
    HealthPillTextField(
        value = draft.name,
        onValueChange = { onDraftNameChange(draft.draftId, it) },
        label = stringResource(R.string.today_meal_food_name),
    )
    NumberFieldRow(
        leftLabel = stringResource(R.string.today_meal_calories),
        leftValue = draft.calories,
        rightLabel = stringResource(R.string.today_meal_protein),
        rightValue = draft.protein,
        onLeftChange = { onDraftCaloriesChange(draft.draftId, it) },
        onRightChange = { onDraftProteinChange(draft.draftId, it) },
    )
    NumberFieldRow(
        leftLabel = stringResource(R.string.today_meal_carbs),
        leftValue = draft.carbs,
        rightLabel = stringResource(R.string.today_meal_fat),
        rightValue = draft.fat,
        onLeftChange = { onDraftCarbsChange(draft.draftId, it) },
        onRightChange = { onDraftFatChange(draft.draftId, it) },
    )
}

@Composable
private fun MealDraftCard(
    draft: MealDraftFoodState,
    state: MealEditorUiState,
    onRemoveDraft: (Long) -> Unit,
    onDraftNameChange: (Long, String) -> Unit,
    onDraftCaloriesChange: (Long, String) -> Unit,
    onDraftProteinChange: (Long, String) -> Unit,
    onDraftCarbsChange: (Long, String) -> Unit,
    onDraftFatChange: (Long, String) -> Unit,
) {
    HealthCard(modifier = Modifier.testTag("meal_draft_${draft.draftId}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.today_meal_food),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (state.draftFoods.size > 1) {
                RoundedPillButton(
                    label = stringResource(R.string.common_delete),
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { onRemoveDraft(draft.draftId) },
                )
            }
        }
        Column(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            MealDraftFields(
                draft = draft,
                onDraftNameChange = onDraftNameChange,
                onDraftCaloriesChange = onDraftCaloriesChange,
                onDraftProteinChange = onDraftProteinChange,
                onDraftCarbsChange = onDraftCarbsChange,
                onDraftFatChange = onDraftFatChange,
            )
        }
    }
}

@Composable
internal fun NumberFieldRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    onLeftChange: (String) -> Unit,
    onRightChange: (String) -> Unit,
    numeric: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        HealthPillTextField(
            modifier = Modifier.weight(1f),
            value = leftValue,
            onValueChange = onLeftChange,
            label = leftLabel,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
            ),
        )
        if (rightLabel.isNotBlank()) {
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = rightValue,
                onValueChange = onRightChange,
                label = rightLabel,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
                ),
            )
        }
    }
}
