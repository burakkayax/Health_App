package com.burak.healthapp.feature.today.meal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing

data class CustomFoodEditorState(
    val id: Long? = null,
    val name: String = "",
    val brand: String = "",
    val servingName: String = "",
    val servingGrams: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val isFavorite: Boolean = false,
    val nameError: String? = null,
    val servingError: String? = null,
    val caloriesError: String? = null,
) {
    val isEditing: Boolean get() = id != null

    val canSave: Boolean
        get() = name.isNotBlank() &&
            servingGrams.toSafeFloat() != null &&
            servingGrams.toSafeFloat()!! > 0f &&
            calories.toSafeInt() != null &&
            calories.toSafeInt()!! >= 0
}

private fun String.toSafeFloat(): Float? = replace(',', '.').toFloatOrNull()
private fun String.toSafeInt(): Int? = replace(',', '.').toFloatOrNull()?.toInt()

@Composable
fun CustomFoodEditorContent(
    state: CustomFoodEditorState,
    onNameChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onServingNameChange: (String) -> Unit,
    onServingGramsChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onBack: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.custom_food_delete_confirm_title)) },
            text = { Text(stringResource(R.string.custom_food_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(
                        stringResource(R.string.custom_food_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .testTag("custom_food_editor"),
        contentPadding = PaddingValues(
            start = HealthSpacing.md,
            end = HealthSpacing.md,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("custom_food_editor_back"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.nutrition_preset_search_back),
                    )
                }
                Text(
                    text = stringResource(
                        if (state.isEditing) {
                            R.string.custom_food_edit_title
                        } else {
                            R.string.custom_food_add_title
                        },
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = HealthSpacing.xs),
                )
            }
        }
        // Local storage note
        item {
            Text(
                text = stringResource(R.string.custom_food_local_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Name
        item {
            HealthPillTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.custom_food_name),
                isError = state.nameError != null,
                supportingText = state.nameError,
            )
        }
        // Brand
        item {
            HealthPillTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.brand,
                onValueChange = onBrandChange,
                label = stringResource(R.string.custom_food_brand_optional),
            )
        }
        // Serving
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.servingName,
                    onValueChange = onServingNameChange,
                    label = stringResource(R.string.custom_food_serving_name),
                )
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.servingGrams,
                    onValueChange = onServingGramsChange,
                    label = stringResource(R.string.custom_food_serving_grams),
                    isError = state.servingError != null,
                    supportingText = state.servingError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
        }
        // Macros
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.calories,
                    onValueChange = onCaloriesChange,
                    label = stringResource(R.string.custom_food_calories),
                    isError = state.caloriesError != null,
                    supportingText = state.caloriesError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.protein,
                    onValueChange = onProteinChange,
                    label = stringResource(R.string.custom_food_protein),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.carbs,
                    onValueChange = onCarbsChange,
                    label = stringResource(R.string.custom_food_carbs),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.fat,
                    onValueChange = onFatChange,
                    label = stringResource(R.string.custom_food_fat),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
        // Save
        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_food_save_button"),
                enabled = state.canSave,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                ),
                onClick = onSave,
                contentPadding = PaddingValues(vertical = HealthSpacing.sm),
            ) {
                Text(text = stringResource(R.string.custom_food_save))
            }
        }
        // Delete (edit mode only)
        if (state.isEditing && onDelete != null) {
            item {
                RoundedPillButton(
                    label = stringResource(R.string.custom_food_delete),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_food_delete_button"),
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteConfirm = true },
                )
            }
        }
    }
}
