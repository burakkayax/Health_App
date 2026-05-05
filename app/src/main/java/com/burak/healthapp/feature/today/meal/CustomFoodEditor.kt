package com.burak.healthapp.feature.today.meal

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
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

// ────────────────────────────────────────────
// Validation error enum — ViewModel carries this, UI maps to stringResource
// ────────────────────────────────────────────
enum class CustomFoodFieldError {
    NAME_REQUIRED,
    SERVING_REQUIRED,
    CALORIES_REQUIRED,
    INVALID_NUMBER,
    NEGATIVE_VALUE,
    VALUE_TOO_LARGE,
}

// ────────────────────────────────────────────
// Submit error for save/delete failures
// ────────────────────────────────────────────
enum class CustomFoodSubmitError {
    SAVE_FAILED,
    DELETE_FAILED,
}

// ────────────────────────────────────────────
// Input parsing helpers (test-friendly, no Android deps)
// ────────────────────────────────────────────
private const val MAX_REASONABLE_VALUE = 99_999

fun parseDecimalInput(value: String): Float? = value.trim().replace(',', '.').toFloatOrNull()

fun validateRequiredPositiveFloat(value: String): CustomFoodFieldError? {
    if (value.isBlank()) return CustomFoodFieldError.SERVING_REQUIRED
    val f = parseDecimalInput(value) ?: return CustomFoodFieldError.INVALID_NUMBER
    if (f < 0f) return CustomFoodFieldError.NEGATIVE_VALUE
    if (f == 0f) return CustomFoodFieldError.SERVING_REQUIRED
    if (f > MAX_REASONABLE_VALUE) return CustomFoodFieldError.VALUE_TOO_LARGE
    return null
}

fun validateRequiredNonNegativeInt(value: String): CustomFoodFieldError? {
    if (value.isBlank()) return CustomFoodFieldError.CALORIES_REQUIRED
    val f = parseDecimalInput(value) ?: return CustomFoodFieldError.INVALID_NUMBER
    if (f < 0f) return CustomFoodFieldError.NEGATIVE_VALUE
    if (f > MAX_REASONABLE_VALUE) return CustomFoodFieldError.VALUE_TOO_LARGE
    return null
}

fun validateOptionalNonNegativeInt(value: String): CustomFoodFieldError? {
    if (value.isBlank()) return null
    val f = parseDecimalInput(value) ?: return CustomFoodFieldError.INVALID_NUMBER
    if (f < 0f) return CustomFoodFieldError.NEGATIVE_VALUE
    if (f > MAX_REASONABLE_VALUE) return CustomFoodFieldError.VALUE_TOO_LARGE
    return null
}

// ────────────────────────────────────────────
// Editor state
// ────────────────────────────────────────────
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
    val preservedFiberGrams: Float? = null,
    val preservedSugarGrams: Float? = null,
    val preservedSodiumMg: Float? = null,
    val isFavorite: Boolean = false,
    val nameError: CustomFoodFieldError? = null,
    val servingError: CustomFoodFieldError? = null,
    val caloriesError: CustomFoodFieldError? = null,
    val proteinError: CustomFoodFieldError? = null,
    val carbsError: CustomFoodFieldError? = null,
    val fatError: CustomFoodFieldError? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val submitError: CustomFoodSubmitError? = null,
) {
    val isEditing: Boolean get() = id != null

    val hasFieldError: Boolean
        get() = nameError != null ||
            servingError != null ||
            caloriesError != null ||
            proteinError != null ||
            carbsError != null ||
            fatError != null

    val canSave: Boolean
        get() = name.isNotBlank() &&
            validateRequiredPositiveFloat(servingGrams) == null &&
            validateRequiredNonNegativeInt(calories) == null &&
            validateOptionalNonNegativeInt(protein) == null &&
            validateOptionalNonNegativeInt(carbs) == null &&
            validateOptionalNonNegativeInt(fat) == null &&
            !isSaving &&
            !isDeleting
}

// ────────────────────────────────────────────
// Error → stringResource mapping
// ────────────────────────────────────────────
@Composable
private fun CustomFoodFieldError.asText(): String = when (this) {
    CustomFoodFieldError.NAME_REQUIRED -> stringResource(R.string.custom_food_error_name_required)
    CustomFoodFieldError.SERVING_REQUIRED -> stringResource(R.string.custom_food_error_serving_required)
    CustomFoodFieldError.CALORIES_REQUIRED -> stringResource(R.string.custom_food_error_calories_required)
    CustomFoodFieldError.INVALID_NUMBER -> stringResource(R.string.custom_food_error_invalid_number)
    CustomFoodFieldError.NEGATIVE_VALUE -> stringResource(R.string.custom_food_error_negative_value)
    CustomFoodFieldError.VALUE_TOO_LARGE -> stringResource(R.string.custom_food_error_value_too_large)
}

@Composable
private fun CustomFoodSubmitError.asText(): String = when (this) {
    CustomFoodSubmitError.SAVE_FAILED -> stringResource(R.string.custom_food_save_error)
    CustomFoodSubmitError.DELETE_FAILED -> stringResource(R.string.custom_food_delete_error)
}

// ────────────────────────────────────────────
// Editor composable
// ────────────────────────────────────────────
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
    onFavoriteChange: (Boolean) -> Unit,
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
                TextButton(
                    enabled = !state.isDeleting,
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                ) {
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
        // Submit error
        val submitErr = state.submitError
        if (submitErr != null) {
            item {
                Text(
                    text = submitErr.asText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("custom_food_submit_error"),
                )
            }
        }
        // Name
        item {
            HealthPillTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.custom_food_name),
                isError = state.nameError != null,
                supportingText = state.nameError?.asText(),
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
                    supportingText = state.servingError?.asText(),
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
                    supportingText = state.caloriesError?.asText(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.protein,
                    onValueChange = onProteinChange,
                    label = stringResource(R.string.custom_food_protein),
                    isError = state.proteinError != null,
                    supportingText = state.proteinError?.asText(),
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
                    isError = state.carbsError != null,
                    supportingText = state.carbsError?.asText(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                HealthPillTextField(
                    modifier = Modifier.weight(1f),
                    value = state.fat,
                    onValueChange = onFatChange,
                    label = stringResource(R.string.custom_food_fat),
                    isError = state.fatError != null,
                    supportingText = state.fatError?.asText(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
        // Favorite
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFavoriteChange(!state.isFavorite) }
                    .padding(vertical = HealthSpacing.xs)
                    .testTag("custom_food_favorite_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.custom_food_favorite),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.isFavorite,
                    onCheckedChange = onFavoriteChange,
                    modifier = Modifier.testTag("custom_food_favorite_switch"),
                )
            }
            Text(
                text = stringResource(R.string.custom_food_favorite_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                    enabled = !state.isDeleting,
                    onClick = { showDeleteConfirm = true },
                )
            }
        }
    }
}
