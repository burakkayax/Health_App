package com.burak.healthapp.feature.today.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.HydrationInputValidator
import com.burak.healthapp.domain.validation.ValidationResult
@Composable
internal fun HydrationSheet(
    onSave: (Int) -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("750") }
    var amountError by rememberSaveable { mutableStateOf<HealthInputError?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.today_sheet_hydration_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HealthPillTextField(
            value = amount,
            onValueChange = {
                amount = it
                if (amountError != null) {
                    amountError = (HydrationInputValidator.validate(it) as? ValidationResult.Invalid)
                        ?.errors
                        ?.firstOrNull()
                }
            },
            label = stringResource(R.string.today_label_amount_ml),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = amountError != null,
            supportingText = amountError?.asString(),
        )
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                when (val result = HydrationInputValidator.validate(amount)) {
                    is ValidationResult.Valid -> onSave(result.value)
                    is ValidationResult.Invalid -> amountError = result.errors.firstOrNull()
                }
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}
