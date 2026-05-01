package com.burak.healthapp

import com.burak.healthapp.core.ui.format.formatWholeNumber
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MetricNumberFormattersTest {
    @Test
    fun formatWholeNumber_usesTurkishGrouping() {
        assertEquals("12.345", formatWholeNumber(12_345, Locale.forLanguageTag("tr-TR")))
        assertEquals("8.750", formatWholeNumber(8_750, Locale.forLanguageTag("tr-TR")))
        assertEquals("100.000", formatWholeNumber(100_000, Locale.forLanguageTag("tr-TR")))
    }

    @Test
    fun formatWholeNumber_usesEnglishGrouping() {
        assertEquals("12,345", formatWholeNumber(12_345, Locale.US))
        assertEquals("8,750", formatWholeNumber(8_750, Locale.US))
        assertEquals("100,000", formatWholeNumber(100_000, Locale.US))
    }

    @Test
    fun formatWholeNumber_handlesZeroAndNegativeValues() {
        assertEquals("0", formatWholeNumber(0, Locale.US))
        assertEquals("-1,000", formatWholeNumber(-1_000, Locale.US))
    }
}
