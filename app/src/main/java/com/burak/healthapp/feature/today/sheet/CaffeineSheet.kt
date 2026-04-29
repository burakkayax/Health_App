package com.burak.healthapp.feature.today.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEstimates

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CaffeineSheet(
    onSave: (CaffeineDrinkType, CaffeineDrinkSize, Int, String?) -> Unit,
) {
    var selectedType by remember { mutableStateOf(CaffeineDrinkType.TURKISH_COFFEE) }
    var selectedSize by remember { mutableStateOf(CaffeineDrinkSize.MEDIUM) }
    var customName by remember { mutableStateOf("") }
    var estimatedMg by remember(selectedType, selectedSize) {
        mutableStateOf(CaffeineEstimates.estimateMg(selectedType, selectedSize).toString())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.sm)
            .testTag("caffeine_sheet"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.caffeine_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.caffeine_estimate_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            quickDrinkTypes.forEach { type ->
                RoundedPillButton(
                    label = type.label(),
                    containerColor = if (selectedType == type) HealthPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedType == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    onClick = { selectedType = type },
                )
            }
        }
        SegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = CaffeineDrinkSize.entries.map { it.label() },
            selectedIndex = CaffeineDrinkSize.entries.indexOf(selectedSize),
            onSelectionChange = { index -> selectedSize = CaffeineDrinkSize.entries[index] },
        )
        HealthPillTextField(
            value = estimatedMg,
            onValueChange = { estimatedMg = it },
            label = stringResource(R.string.caffeine_estimated_mg),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        if (selectedType == CaffeineDrinkType.OTHER) {
            HealthPillTextField(
                value = customName,
                onValueChange = { customName = it },
                label = stringResource(R.string.caffeine_custom_name),
            )
        }
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("caffeine_save_button"),
            containerColor = HealthPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = {
                val parsedMg = estimatedMg.toIntOrNull()?.takeIf { it > 0 } ?: return@RoundedPillButton
                onSave(
                    selectedType,
                    selectedSize,
                    parsedMg,
                    customName.trim().ifBlank { null },
                )
            },
        )
    }
}

private val quickDrinkTypes = listOf(
    CaffeineDrinkType.TURKISH_COFFEE,
    CaffeineDrinkType.FILTER_COFFEE,
    CaffeineDrinkType.ESPRESSO,
    CaffeineDrinkType.BLACK_TEA,
    CaffeineDrinkType.ENERGY_DRINK,
    CaffeineDrinkType.COLA,
    CaffeineDrinkType.OTHER,
)

@Composable
internal fun CaffeineDrinkType.label(): String = when (this) {
    CaffeineDrinkType.TURKISH_COFFEE -> stringResource(R.string.caffeine_type_turkish_coffee)
    CaffeineDrinkType.FILTER_COFFEE -> stringResource(R.string.caffeine_type_filter_coffee)
    CaffeineDrinkType.ESPRESSO -> stringResource(R.string.caffeine_type_espresso)
    CaffeineDrinkType.AMERICANO -> stringResource(R.string.caffeine_type_americano)
    CaffeineDrinkType.LATTE -> stringResource(R.string.caffeine_type_latte)
    CaffeineDrinkType.CAPPUCCINO -> stringResource(R.string.caffeine_type_cappuccino)
    CaffeineDrinkType.BLACK_TEA -> stringResource(R.string.caffeine_type_black_tea)
    CaffeineDrinkType.GREEN_TEA -> stringResource(R.string.caffeine_type_green_tea)
    CaffeineDrinkType.ENERGY_DRINK -> stringResource(R.string.caffeine_type_energy_drink)
    CaffeineDrinkType.COLA -> stringResource(R.string.caffeine_type_cola)
    CaffeineDrinkType.OTHER -> stringResource(R.string.caffeine_type_other)
}

@Composable
internal fun CaffeineDrinkSize.label(): String = when (this) {
    CaffeineDrinkSize.SMALL -> stringResource(R.string.caffeine_size_small)
    CaffeineDrinkSize.MEDIUM -> stringResource(R.string.caffeine_size_medium)
    CaffeineDrinkSize.LARGE -> stringResource(R.string.caffeine_size_large)
}
