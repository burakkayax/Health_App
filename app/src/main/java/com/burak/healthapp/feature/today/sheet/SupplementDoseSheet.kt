package com.burak.healthapp.feature.today.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.SupplementDoseValidator
import com.burak.healthapp.domain.validation.ValidationResult
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.formatFloat
import java.time.LocalDate
@Composable
internal fun SupplementDoseSheet(
    items: List<SupplementItemState>,
    onSave: (List<SupplementDoseEntry>) -> Unit,
) {
    val initialDrafts = remember(items) {
        items.map { item ->
            SupplementDoseDraft(
                templateId = item.id,
                name = item.name,
                unitLabel = item.unitLabel,
                amountText = if (item.currentAmount > 0f) formatFloat(item.currentAmount) else "",
            )
        }
    }
    var editableDrafts by remember(items) { mutableStateOf(initialDrafts) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.today_sheet_supplement_dose_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        editableDrafts.forEachIndexed { index, item ->
            HealthPillTextField(
                value = item.amountText,
                onValueChange = { newValue ->
                    editableDrafts = editableDrafts.toMutableList().also { list ->
                        list[index] = list[index].copy(amountText = newValue, error = null)
                    }
                },
                label = item.name,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = item.error != null,
                supportingText = item.error?.asString(),
                suffix = {
                    Text(
                        text = item.unitLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                val validatedDrafts = editableDrafts.map { item ->
                    if (item.amountText.isBlank()) {
                        item.copy(error = null)
                    } else {
                        when (val result = SupplementDoseValidator.validateAmount(item.amountText)) {
                            is ValidationResult.Valid -> item.copy(error = null)
                            is ValidationResult.Invalid -> item.copy(error = result.errors.firstOrNull())
                        }
                    }
                }
                editableDrafts = validatedDrafts
                if (validatedDrafts.any { it.error != null }) return@RoundedPillButton

                onSave(
                    validatedDrafts.mapNotNull { item ->
                        val amount = (SupplementDoseValidator.validateAmount(item.amountText) as? ValidationResult.Valid)?.value
                        amount?.let {
                            SupplementDoseEntry(
                                templateId = item.templateId,
                                date = LocalDate.now(),
                                amount = it,
                            )
                        }
                    },
                )
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

private data class SupplementDoseDraft(
    val templateId: Long,
    val name: String,
    val unitLabel: String,
    val amountText: String,
    val error: HealthInputError? = null,
)
