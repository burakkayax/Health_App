package com.burak.healthapp.feature.today.meal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SkeletonCard
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.model.nutrition.NutritionDataQualityLevel
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import kotlin.math.roundToInt

/**
 * Route-level composable that wires the ViewModel to [MealFoodSearchContent].
 * This is NOT a standalone ModalBottomSheet — it is hosted inside the Meal bottom sheet.
 */
@Composable
fun MealFoodSearchRoute(
    onBack: () -> Unit,
    onSelectPreset: (NutritionPresetAutofillState) -> Unit,
    onSelectCustomFood: (CustomFood) -> Unit,
    onAddCustomFood: () -> Unit,
    onEditCustomFood: (Long) -> Unit,
    viewModel: MealFoodSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MealFoodSearchContent(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onCategoryChange = viewModel::onCategoryChange,
        onSourceFilterChange = viewModel::onSourceFilterChange,
        onSelectPreset = { food ->
            onSelectPreset(food.defaultAutofill())
        },
        onSelectCustomFood = onSelectCustomFood,
        onAddCustomFood = onAddCustomFood,
        onEditCustomFood = onEditCustomFood,
    )
}

@Composable
fun MealFoodSearchContent(
    state: MealFoodSearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onSourceFilterChange: (FoodSearchSourceFilter) -> Unit,
    onSelectPreset: (NutritionPresetFood) -> Unit,
    onSelectCustomFood: (CustomFood) -> Unit,
    onAddCustomFood: () -> Unit,
    onEditCustomFood: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .testTag("nutrition_preset_search_sheet"),
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HealthSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("nutrition_preset_search_back"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nutrition_preset_search_back),
                )
            }
            Text(
                text = stringResource(R.string.nutrition_preset_search_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = HealthSpacing.xs),
            )
        }

        // Scrollable content using a single LazyColumn for proper height management
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = HealthSpacing.sm,
                end = HealthSpacing.sm,
                bottom = HealthSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                Text(
                    text = stringResource(R.string.nutrition_preset_search_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nutrition_preset_search_input"),
                    value = state.query,
                    onValueChange = onQueryChange,
                    label = { Text(stringResource(R.string.nutrition_preset_search_label)) },
                    singleLine = true,
                )
            }
            // Source filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedSource == FoodSearchSourceFilter.ALL,
                            onClick = { onSourceFilterChange(FoodSearchSourceFilter.ALL) },
                            label = { Text(stringResource(R.string.food_search_filter_all)) },
                            modifier = Modifier.testTag("food_search_filter_all"),
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedSource == FoodSearchSourceFilter.PRESETS,
                            onClick = { onSourceFilterChange(FoodSearchSourceFilter.PRESETS) },
                            label = { Text(stringResource(R.string.food_search_filter_presets)) },
                            modifier = Modifier.testTag("food_search_filter_presets"),
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedSource == FoodSearchSourceFilter.CUSTOM,
                            onClick = { onSourceFilterChange(FoodSearchSourceFilter.CUSTOM) },
                            label = { Text(stringResource(R.string.food_search_filter_custom)) },
                            modifier = Modifier.testTag("food_search_filter_custom"),
                        )
                    }
                }
            }
            // Category chips (only visible when showing presets)
            if (state.selectedSource != FoodSearchSourceFilter.CUSTOM && state.categories.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedCategory == null,
                                onClick = { onCategoryChange(null) },
                                label = { Text(stringResource(R.string.nutrition_preset_search_all_categories)) },
                                modifier = Modifier.testTag("nutrition_preset_category_all"),
                            )
                        }
                        items(state.categories) { category ->
                            FilterChip(
                                selected = state.selectedCategory == category,
                                onClick = { onCategoryChange(category) },
                                label = { Text(category) },
                                modifier = Modifier.testTag(
                                    "nutrition_preset_category_${category.replace(Regex("[^a-zA-Z0-9]"), "_")}",
                                ),
                            )
                        }
                    }
                }
            }
            // Add custom food button
            item {
                RoundedPillButton(
                    label = stringResource(R.string.custom_food_add_button),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_food_add_button"),
                    containerColor = HealthPrimary.copy(alpha = 0.12f),
                    contentColor = HealthPrimary,
                    onClick = onAddCustomFood,
                )
            }
            // Results
            if (state.isLoading) {
                items(3) { SkeletonCard(lines = 2) }
            } else if (state.isError) {
                item {
                    Text(
                        modifier = Modifier.padding(HealthSpacing.sm),
                        text = stringResource(R.string.nutrition_preset_search_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                // Custom foods first
                if (state.customResults.isNotEmpty()) {
                    items(state.customResults, key = { "custom_${it.id}" }) { food ->
                        CustomFoodResultRow(
                            food = food,
                            onClick = { onSelectCustomFood(food) },
                            onEdit = { onEditCustomFood(food.id) },
                        )
                    }
                }
                // Then preset results
                if (state.presetResults.isNotEmpty()) {
                    items(state.presetResults, key = { "preset_${it.id}" }) { food ->
                        PresetFoodResultRow(
                            food = food,
                            onClick = { onSelectPreset(food) },
                        )
                    }
                }
                if (state.customResults.isEmpty() && state.presetResults.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(HealthSpacing.sm),
                            text = stringResource(R.string.nutrition_preset_search_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetFoodResultRow(
    food: NutritionPresetFood,
    onClick: () -> Unit,
) {
    val serving = food.defaultServing
    val nutrients = food.nutrientsPerDefaultServing
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("nutrition_preset_result_${food.id}"),
    ) {
        Text(
            text = food.nameTr,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = "${serving.nameTr} · ${formatWholeNumber(serving.grams.roundToInt())} g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = "${formatWholeNumber(nutrients.energyKcal.roundToInt())} kcal · P ${nutrients.proteinG.roundToInt()}g · K ${nutrients.carbsG.roundToInt()}g · Y ${nutrients.fatG.roundToInt()}g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val qualityRes = when (food.dataQuality.level) {
            NutritionDataQualityLevel.HIGH -> R.string.nutrition_quality_high
            NutritionDataQualityLevel.MEDIUM -> R.string.nutrition_quality_medium
            NutritionDataQualityLevel.LOW -> R.string.nutrition_quality_low
        }
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = "${food.categoryTr} · ${stringResource(qualityRes)} · ${stringResource(R.string.custom_food_source_preset)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CustomFoodResultRow(
    food: CustomFood,
    onClick: () -> Unit,
    onEdit: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("custom_food_result_${food.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            RoundedPillButton(
                label = stringResource(R.string.custom_food_edit_title),
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.secondary,
                onClick = onEdit,
                modifier = Modifier.testTag("custom_food_edit_${food.id}"),
            )
        }
        val brand = food.brand
        if (!brand.isNullOrBlank()) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = brand,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = "${food.servingName} · ${food.servingGrams.roundToInt()} g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = "${food.calories} kcal · P ${food.proteinGrams}g · K ${food.carbsGrams}g · Y ${food.fatGrams}g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.custom_food_source_custom),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
