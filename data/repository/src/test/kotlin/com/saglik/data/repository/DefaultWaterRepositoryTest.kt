package com.saglik.data.repository

import com.saglik.core.database.dao.WaterDao
import com.saglik.core.database.entity.WaterEntryEntity
import com.saglik.domain.water.AddWaterEntryInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DefaultWaterRepositoryTest {

    private val waterDao: WaterDao = mock()
    private val repository = DefaultWaterRepository(waterDao)

    @Test
    fun addWaterEntry_callsDao() = runTest {
        val input = AddWaterEntryInput(amountMl = 250, recordedAtMillis = 1000L)
        val result = repository.addWaterEntry(input)

        assertTrue(result.isSuccess)
        verify(waterDao).insert(any())
    }

    @Test
    fun observeWaterEntries_mapsEntitiesToDomain() = runTest {
        val entities = listOf(
            WaterEntryEntity("1", 250, 1000L, null, "USER_ENTERED", null)
        )
        whenever(waterDao.getWaterEntries(0, 2000)).thenReturn(flowOf(entities))

        val entries = repository.observeWaterEntries(0, 2000).first()

        assertEquals(1, entries.size)
        assertEquals(250, entries[0].amountMl)
    }
}
