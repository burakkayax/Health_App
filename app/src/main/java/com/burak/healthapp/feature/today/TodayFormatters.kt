package com.burak.healthapp.feature.today

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatFloat(value: Float): String = if (value % 1f == 0f) {
    value.toInt().toString()
} else {
    String.format(Locale.US, "%.1f", value)
}

internal fun String.toIntOrDefault(default: Int): Int = toIntOrNull() ?: default
internal fun String.toFloatOrDefault(default: Float): Float = toFloatOrNull() ?: default

internal fun String.toLocalTimeOrNull(): LocalTime? = runCatching {
    LocalTime.parse(this, DateTimeFormatter.ofPattern("H:mm"))
}.getOrNull()
