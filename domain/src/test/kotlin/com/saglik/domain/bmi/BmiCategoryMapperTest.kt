package com.saglik.domain.bmi

import org.junit.Assert.assertEquals
import org.junit.Test

class BmiCategoryMapperTest {
    private val mapper = BmiCategoryMapper()

    @Test
    fun mapsLowBoundary() {
        assertEquals(BmiCategory.LOW, mapper.map(18.4f))
    }

    @Test
    fun mapsHealthyStartBoundary() {
        assertEquals(BmiCategory.HEALTHY, mapper.map(18.5f))
    }

    @Test
    fun mapsHealthyUpperRange() {
        assertEquals(BmiCategory.HEALTHY, mapper.map(24.9f))
    }

    @Test
    fun mapsHighStartBoundary() {
        assertEquals(BmiCategory.HIGH, mapper.map(25.0f))
    }

    @Test
    fun mapsHighUpperRange() {
        assertEquals(BmiCategory.HIGH, mapper.map(29.9f))
    }

    @Test
    fun mapsVeryHighStartBoundary() {
        assertEquals(BmiCategory.VERY_HIGH, mapper.map(30.0f))
    }

    @Test
    fun mapsVeryHighRange() {
        assertEquals(BmiCategory.VERY_HIGH, mapper.map(40.0f))
    }
}
