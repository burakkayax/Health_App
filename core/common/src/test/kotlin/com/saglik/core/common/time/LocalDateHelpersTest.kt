package com.saglik.core.common.time

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalDateHelpersTest {

    @Test
    fun startOfIsoWeekReturnsMonday() {
        val friday = LocalDate(2026, 6, 26)

        assertEquals(LocalDate(2026, 6, 22), friday.startOfIsoWeek())
    }
}
