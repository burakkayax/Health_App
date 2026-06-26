package com.saglik.core.common.time

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object SleepTimeTextFormatter {
    private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    fun format(rawText: String): String {
        val digits = rawText.filter { it.isDigit() }.take(4)
        return when (digits.length) {
            0, 1, 2 -> digits
            3 -> "${digits.take(2)}:${digits.drop(2)}"
            else -> "${digits.take(2)}:${digits.drop(2)}"
        }
    }

    fun isComplete(text: String): Boolean =
        format(text).length == 5

    fun parseOrNull(text: String): LocalTime? {
        val formatted = format(text)
        if (formatted.length != 5) return null
        return try {
            LocalTime.parse(formatted, TimeFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
