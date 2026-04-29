package com.burak.healthapp.core.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.core.ui.R
import com.burak.healthapp.domain.validation.HealthInputError

@Composable
fun HealthInputError.asString(): String = when (this) {
    HealthInputError.REQUIRED -> stringResource(R.string.error_field_required)
    HealthInputError.MUST_BE_NUMBER -> stringResource(R.string.error_field_must_be_number)
    HealthInputError.MUST_BE_INTEGER -> stringResource(R.string.error_field_must_be_integer)
    HealthInputError.MUST_BE_POSITIVE -> stringResource(R.string.error_field_positive)
    HealthInputError.MUST_NOT_BE_NEGATIVE -> stringResource(R.string.error_field_not_negative)
    HealthInputError.TOO_HIGH -> stringResource(R.string.error_field_too_high)
    HealthInputError.TOO_LOW -> stringResource(R.string.error_field_too_low)
    HealthInputError.INVALID_TIME -> stringResource(R.string.error_field_invalid_time)
    HealthInputError.DURATION_TOO_LONG -> stringResource(R.string.error_sleep_duration_too_long)
}
