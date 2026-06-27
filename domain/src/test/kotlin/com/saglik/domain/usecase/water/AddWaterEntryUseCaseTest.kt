package com.saglik.domain.usecase.water

import com.saglik.domain.repository.WaterRepository
import com.saglik.domain.water.AddWaterEntryInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AddWaterEntryUseCaseTest {

    private val repository: WaterRepository = mock()
    private val validator = ValidateWaterEntryInputUseCase()
    private val useCase = AddWaterEntryUseCase(repository, validator)

    @Test
    fun validEntry_callsRepository() = runTest {
        val input = AddWaterEntryInput(250, 1000L)
        whenever(repository.addWaterEntry(input)).thenReturn(Result.success(Unit))

        val result = useCase(input)

        assertTrue(result.isSuccess)
        verify(repository).addWaterEntry(input)
    }

    @Test
    fun invalidEntry_returnsFailure() = runTest {
        val input = AddWaterEntryInput(-10, 1000L)
        val result = useCase(input)

        assertTrue(result.isFailure)
    }
}
