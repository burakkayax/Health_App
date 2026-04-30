package com.burak.healthapp.feature.today

import com.burak.healthapp.domain.validation.parseLocalizedDecimalInput
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatFloat(value: Float): String = if (value % 1f == 0f) {
    value.toInt().toString()
} else {
    String.format(Locale.US, "%.1f", value)
}

internal fun String.toIntOrDefault(default: Int): Int = toIntOrNull() ?: default
internal fun String.toFloatOrDefault(default: Float): Float = parseLocalizedDecimalInput(this) ?: default

internal fun String.toLocalTimeOrNull(): LocalTime? = runCatching {
    LocalTime.parse(this, DateTimeFormatter.ofPattern("H:mm"))
}.getOrNull()
