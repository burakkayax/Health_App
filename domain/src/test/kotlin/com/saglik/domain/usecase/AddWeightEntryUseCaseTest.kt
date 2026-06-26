@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddWeightEntryUseCaseTest {
    @Test
    fun validWeightPersistsEntry() = runBlocking {
        val repository = AddWeightFakeRepository()
        val useCase = AddWeightEntryUseCase(
            repository = repository,
            nowProvider = { Instant.fromEpochMilliseconds(100) },
            idProvider = { "id" },
        )

        val saved = useCase(65.5f)

        assertTrue(saved)
        assertEquals(1, repository.savedEntries.size)
        assertEquals(65.5f, repository.savedEntries.single().weightKg, 0.001f)
    }

    @Test
    fun outOfRangeWeightIsRejected() = runBlocking {
        val repository = AddWeightFakeRepository()
        val useCase = AddWeightEntryUseCase(repository)

        assertFalse(useCase(29.9f))
        assertFalse(useCase(300.1f))
        assertTrue(repository.savedEntries.isEmpty())
    }
}

private class AddWeightFakeRepository : WeightRepository {
    val savedEntries = mutableListOf<WeightEntry>()
    private val latestEntry = MutableStateFlow<WeightEntry?>(null)
    private val entries = MutableStateFlow<List<WeightEntry>>(emptyList())

    override fun observeLatestWeightEntry(): Flow<WeightEntry?> = latestEntry

    override fun observeWeightEntries(): Flow<List<WeightEntry>> = entries

    override suspend fun addWeightEntry(entry: WeightEntry) {
        savedEntries += entry
        latestEntry.value = entry
        entries.value = savedEntries.toList()
    }
}
