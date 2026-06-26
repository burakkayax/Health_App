@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSleepEntryUseCaseTest {
    private val now = Instant.parse("2026-06-26T12:00:00Z")

    @Test
    fun validInputSavesSleepEntry() = runBlocking {
        val repository = FakeSleepRepository()
        val useCase = AddSleepEntryUseCase(
            repository = repository,
            validateSleepInputUseCase = ValidateSleepInputUseCase(nowProvider = { now }),
            idProvider = { "sleep-id" },
        )

        val saved = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-25T23:50:00Z"),
                endTime = Instant.parse("2026-06-26T07:14:00Z"),
                quality = null,
                note = "Restful",
            ),
        )

        val entry = repository.savedEntries.single()
        assertTrue(saved)
        assertEquals("sleep-id", entry.id)
        assertEquals(DataSource.MANUAL, entry.source)
        assertEquals(444, entry.durationMinutes)
        assertEquals("Restful", entry.note)
    }

    @Test
    fun invalidInputDoesNotSave() = runBlocking {
        val repository = FakeSleepRepository()
        val useCase = AddSleepEntryUseCase(
            repository = repository,
            validateSleepInputUseCase = ValidateSleepInputUseCase(nowProvider = { now }),
        )

        val saved = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-26T07:00:00Z"),
                endTime = Instant.parse("2026-06-26T07:10:00Z"),
                quality = null,
                note = null,
            ),
        )

        assertFalse(saved)
        assertTrue(repository.savedEntries.isEmpty())
    }
}
