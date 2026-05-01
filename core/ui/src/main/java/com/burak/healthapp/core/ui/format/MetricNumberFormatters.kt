package com.burak.healthapp.core.ui.format

import java.text.NumberFormat
import java.util.Locale

fun formatWholeNumber(
    value: Int,
    locale: Locale = Locale.getDefault(),
): String = formatWholeNumber(value.toLong(), locale)

fun formatWholeNumber(
    value: Long,
    locale: Locale = Locale.getDefault(),
): String = NumberFormat.getIntegerInstance(locale).format(value)

fun formatMetricCount(
    value: Int,
    locale: Locale = Locale.getDefault(),
): String = formatWholeNumber(value, locale)
