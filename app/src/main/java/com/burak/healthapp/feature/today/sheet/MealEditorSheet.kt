package com.burak.healthapp.feature.today.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.feature.today.meal.MealDraftFoodState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.today.meal.MealTotalSummary
import com.burak.healthapp.feature.today.meal.labelResId
@Composable
internal fun MealEditorSheet(
    state: MealEditorUiState,
    onMealTypeChange: (MealType) -> Unit,
    onAddDraft: () -> Unit,
    onRemoveDraft: (Long) -> Unit,
    onSearchFood: (Long) -> Unit,
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
                        label = { Text(stringResource(mealType.labelResId)) },
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
                    Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                        RoundedPillButton(
                            label = stringResource(R.string.nutrition_preset_search_title),
                            modifier = Modifier.testTag("nutrition_preset_search_button_${draft.draftId}"),
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.secondary,
                            onClick = { onSearchFood(draft.draftId) },
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
        if (state.totalSummary.foodCount > 0 || state.totalSummary.hasInvalidDrafts) {
            item {
                MealTotalSummaryBar(
                    summary = state.totalSummary,
                )
            }
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

@Composable
internal fun MealTotalSummaryBar(
    summary: MealTotalSummary,
    modifier: Modifier = Modifier,
) {
    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meal_total_summary_bar"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.today_meal_food_count, summary.foodCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(
                R.string.today_meal_total_summary,
                summary.totalCalories,
                summary.totalProtein,
                summary.totalCarbs,
                summary.totalFat,
            ),
            style = MaterialTheme.typography.titleSmall,
            color = HealthPrimary,
        )
        if (summary.hasInvalidDrafts) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(R.string.today_meal_total_invalid_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
