package com.burak.healthapp.feature.today.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.validation.ExerciseInputValidator
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.ValidationResult
import com.burak.healthapp.feature.today.components.toExerciseIcon
import com.burak.healthapp.feature.today.labelResId

@Composable
internal fun ExerciseEditorSheet(
    currentType: ExerciseType?,
    currentDuration: Int,
    currentIntensity: ExerciseIntensity?,
    onSave: (ExerciseType, Int, ExerciseIntensity) -> Unit,
) {
    var selectedType by rememberSaveable { mutableStateOf(currentType ?: ExerciseType.WEIGHTS) }
    var selectedDuration by rememberSaveable { mutableStateOf(currentDuration.takeIf { it > 0 } ?: 45) }
    var useCustomDuration by rememberSaveable { mutableStateOf(currentDuration !in listOf(30, 45, 60) && currentDuration > 0) }
    var customDuration by rememberSaveable { mutableStateOf(if (useCustomDuration) currentDuration.toString() else "") }
    var selectedIntensity by rememberSaveable { mutableStateOf(currentIntensity ?: ExerciseIntensity.MEDIUM) }
    var durationError by rememberSaveable { mutableStateOf<HealthInputError?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.today_sheet_exercise_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
            items(ExerciseType.entries) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = type.toExerciseIcon(),
                                contentDescription = null,
                            )
                            Text(stringResource(type.labelResId))
                        }
                    },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            listOf(30, 45, 60).forEach { duration ->
                FilterChip(
                    selected = !useCustomDuration && selectedDuration == duration,
                    onClick = {
                        useCustomDuration = false
                        selectedDuration = duration
                    },
                    label = { Text(stringResource(R.string.today_format_duration_minutes, duration)) },
                )
            }
            FilterChip(
                selected = useCustomDuration,
                onClick = { useCustomDuration = true },
                label = { Text(stringResource(R.string.today_label_custom)) },
            )
        }
        if (useCustomDuration) {
            HealthPillTextField(
                value = customDuration,
                onValueChange = {
                    customDuration = it
                    durationError = null
                },
                label = stringResource(R.string.today_label_custom_duration),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = durationError != null,
                supportingText = durationError?.asString(),
            )
        }
        SegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = ExerciseIntensity.entries.map { stringResource(it.labelResId) },
            selectedIndex = ExerciseIntensity.entries.indexOf(selectedIntensity),
            onSelectionChange = { index ->
                selectedIntensity = ExerciseIntensity.entries[index]
            },
        )
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                val durationText = if (useCustomDuration) customDuration else selectedDuration.toString()
                when (val result = ExerciseInputValidator.validateDuration(durationText)) {
                    is ValidationResult.Valid -> onSave(selectedType, result.value, selectedIntensity)
                    is ValidationResult.Invalid -> durationError = result.errors.firstOrNull()
                }
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}
