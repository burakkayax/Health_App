package com.saglik.domain.sleep

import com.saglik.core.model.SleepQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepQualityMapperTest {
    private val mapper = SleepQualityMapper()

    @Test
    fun mapsDurationToQuality() {
        assertEquals(SleepQuality.POOR, mapper.map(299))
        assertEquals(SleepQuality.OKAY, mapper.map(300))
        assertEquals(SleepQuality.OKAY, mapper.map(389))
        assertEquals(SleepQuality.GOOD, mapper.map(390))
        assertEquals(SleepQuality.GOOD, mapper.map(510))
        assertEquals(SleepQuality.EXCELLENT, mapper.map(511))
    }
}
