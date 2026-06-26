package com.saglik.core.common.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimeTextFormatterTest {
    @Test
    fun formatsProgressivelyWithoutShiftingTimeSegments() {
        assertEquals("0", SleepTimeTextFormatter.format("0"))
        assertEquals("05", SleepTimeTextFormatter.format("05"))
        assertEquals("05:0", SleepTimeTextFormatter.format("050"))
        assertEquals("05:00", SleepTimeTextFormatter.format("0500"))
    }

    @Test
    fun supportsRequiredValidTimes() {
        assertEquals("05:00", SleepTimeTextFormatter.format("0500"))
        assertEquals("00:30", SleepTimeTextFormatter.format("0030"))
        assertEquals("23:50", SleepTimeTextFormatter.format("2350"))
        assertEquals("07:14", SleepTimeTextFormatter.format("0714"))

        assertEquals(5, SleepTimeTextFormatter.parseOrNull("05:00")?.hour)
        assertEquals(30, SleepTimeTextFormatter.parseOrNull("00:30")?.minute)
        assertEquals(23, SleepTimeTextFormatter.parseOrNull("23:50")?.hour)
        assertEquals(14, SleepTimeTextFormatter.parseOrNull("07:14")?.minute)
    }

    @Test
    fun malformedColonValuesAreRemasked() {
        val formatted = SleepTimeTextFormatter.format("0:500")

        assertEquals("05:00", formatted)
        assertNotEquals("0:500", formatted)
        assertTrue(SleepTimeTextFormatter.isComplete(formatted))
    }

    @Test
    fun invalidStructuredTimesDoNotParse() {
        assertNull(SleepTimeTextFormatter.parseOrNull("29:99"))
        assertNull(SleepTimeTextFormatter.parseOrNull("05:0"))
    }
}
