package com.saglik.domain.bmi

import org.junit.Assert.assertEquals
import org.junit.Test

class BmiFormatterTest {
    @Test
    fun formatsValueToOneDecimalPlace() {
        assertEquals("24.1", BmiFormatter.formatValue(24.123f))
    }

    @Test
    fun mapsHealthyLabel() {
        assertEquals("Healthy range", BmiFormatter.categoryLabel(BmiCategory.HEALTHY))
    }

    @Test
    fun mapsLowLabel() {
        assertEquals("Low range", BmiFormatter.categoryLabel(BmiCategory.LOW))
    }
}
