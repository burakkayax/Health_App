package com.burak.healthapp.core.ui.format

import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private val integerFormatters = ConcurrentHashMap<String, ThreadLocal<NumberFormat>>()

fun formatWholeNumber(
    value: Int,
    locale: Locale = Locale.getDefault(),
): String = formatWholeNumber(value.toLong(), locale)

fun formatWholeNumber(
    value: Long,
    locale: Locale = Locale.getDefault(),
): String = integerFormatter(locale).format(value)

fun formatMetricCount(
    value: Int,
    locale: Locale = Locale.getDefault(),
): String = formatWholeNumber(value, locale)

private fun integerFormatter(locale: Locale): NumberFormat =
    checkNotNull(
        integerFormatters
        .getOrPut(locale.toLanguageTag()) {
            ThreadLocal.withInitial { NumberFormat.getIntegerInstance(locale) }
        }
            .get(),
    )
