package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.data.nutrition.TurkishSearchNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TurkishSearchNormalizerTest {
    @Test
    fun `normalizes Turkish characters`() {
        assertEquals("sut", TurkishSearchNormalizer.normalize("Süt"))
        assertEquals("yogurt", TurkishSearchNormalizer.normalize("Yoğurt"))
        assertEquals("cig", TurkishSearchNormalizer.normalize("Çiğ"))
    }

    @Test
    fun `normalizes dotted capital I`() {
        assertEquals("istanbul", TurkishSearchNormalizer.normalize("İstanbul"))
    }

    @Test
    fun `normalizes dotless lowercase i`() {
        assertEquals("isik", TurkishSearchNormalizer.normalize("ışık"))
    }

    @Test
    fun `preserves basic ascii`() {
        assertEquals("tavuk gogsu", TurkishSearchNormalizer.normalize("tavuk göğsü"))
    }

    @Test
    fun `trims whitespace`() {
        assertEquals("test", TurkishSearchNormalizer.normalize("  test  "))
    }

    @Test
    fun `collapses multiple spaces`() {
        assertEquals("a b", TurkishSearchNormalizer.normalize("a   b"))
    }

    @Test
    fun `blank input returns blank`() {
        assertTrue(TurkishSearchNormalizer.normalize("").isBlank())
        assertTrue(TurkishSearchNormalizer.normalize("   ").isBlank())
    }

    @Test
    fun `removes accents from non-Turkish characters`() {
        assertEquals("cafe", TurkishSearchNormalizer.normalize("café"))
    }

    @Test
    fun `handles mixed case`() {
        assertEquals("balik", TurkishSearchNormalizer.normalize("BALIK"))
    }
}
