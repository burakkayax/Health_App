package com.saglik.domain.bmi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BmiCalculatorTest {
    private val calculator = BmiCalculator()

    @Test
    fun height185AndWeight824ReturnsApproximately241() {
        val result = calculator.calculate(weightKg = 82.4f, heightCm = 185f)

        assertEquals(24.1f, result ?: 0f, 0.05f)
    }

    @Test
    fun height180AndWeight81Returns25() {
        val result = calculator.calculate(weightKg = 81f, heightCm = 180f)

        assertEquals(25.0f, result ?: 0f, 0.05f)
    }

    @Test
    fun zeroHeightReturnsNull() {
        assertNull(calculator.calculate(weightKg = 82.4f, heightCm = 0f))
    }

    @Test
    fun negativeHeightReturnsNull() {
        assertNull(calculator.calculate(weightKg = 82.4f, heightCm = -185f))
    }

    @Test
    fun zeroWeightReturnsNull() {
        assertNull(calculator.calculate(weightKg = 0f, heightCm = 185f))
    }

    @Test
    fun negativeWeightReturnsNull() {
        assertNull(calculator.calculate(weightKg = -82.4f, heightCm = 185f))
    }
}
