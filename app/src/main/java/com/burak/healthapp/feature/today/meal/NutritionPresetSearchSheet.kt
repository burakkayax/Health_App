package com.burak.healthapp.feature.today.meal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.burak.healthapp.core.ui.components.SkeletonCard
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.nutrition.NutritionDataQualityLevel
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import kotlin.math.roundToInt

/**
 * Route-level composable that wires the ViewModel to [NutritionPresetSearchContent].
 * This is NOT a standalone ModalBottomSheet — it is hosted inside the Meal bottom sheet.
 */
@Composable
fun NutritionPresetSearchRoute(
    onBack: () -> Unit,
    onSelectPreset: (NutritionPresetAutofillState) -> Unit,
    viewModel: NutritionPresetSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NutritionPresetSearchContent(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onCategoryChange = viewModel::onCategoryChange,
        onSelectPreset = { food ->
            onSelectPreset(food.defaultAutofill())
        },
    )
}

@Composable
fun NutritionPresetSearchContent(
    state: NutritionPresetSearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onSelectPreset: (NutritionPresetFood) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = HealthSpacing.sm)
            .testTag("nutrition_preset_search_sheet"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = null,
                )
            }
            Text(
                text = stringResource(R.string.nutrition_preset_search_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = HealthSpacing.xs),
            )
        }
        Text(
            text = stringResource(R.string.nutrition_preset_search_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("nutrition_preset_search_input"),
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.nutrition_preset_search_label)) },
            singleLine = true,
        )
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
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
            } else if (state.results.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(HealthSpacing.sm),
                        text = stringResource(R.string.nutrition_preset_search_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                items(state.results, key = { it.id }) { food ->
                    NutritionPresetResultRow(
                        food = food,
                        onClick = { onSelectPreset(food) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionPresetResultRow(
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
            text = "${food.categoryTr} · ${stringResource(qualityRes)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
