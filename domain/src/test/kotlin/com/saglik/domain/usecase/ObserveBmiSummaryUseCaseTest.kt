@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex
import com.saglik.core.model.UserProfile
import com.saglik.core.model.WeightEntry
import com.saglik.domain.bmi.BmiCategory
import com.saglik.domain.bmi.BmiMissingReason
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveBmiSummaryUseCaseTest {
    @Test
    fun profileAndLatestWeightReturnsBmi() = runBlocking {
        val useCase = ObserveBmiSummaryUseCase(
            userProfileRepository = BmiFakeUserProfileRepository(profile(heightCm = 185f)),
            weightRepository = BmiFakeWeightRepository(weight(weightKg = 82.4f)),
        )

        val result = useCase().first()

        assertTrue(result.hasData)
        assertEquals(24.1f, result.bmi?.value ?: 0f, 0.05f)
        assertEquals(BmiCategory.HEALTHY, result.bmi?.category)
        assertNull(result.missingReason)
    }

    @Test
    fun missingProfileReturnsMissingProfile() = runBlocking {
        val useCase = ObserveBmiSummaryUseCase(
            userProfileRepository = BmiFakeUserProfileRepository(null),
            weightRepository = BmiFakeWeightRepository(weight(weightKg = 82.4f)),
        )

        val result = useCase().first()

        assertFalse(result.hasData)
        assertEquals(BmiMissingReason.MISSING_PROFILE, result.missingReason)
    }

    @Test
    fun invalidHeightReturnsMissingHeight() = runBlocking {
        val useCase = ObserveBmiSummaryUseCase(
            userProfileRepository = BmiFakeUserProfileRepository(profile(heightCm = 0f)),
            weightRepository = BmiFakeWeightRepository(weight(weightKg = 82.4f)),
        )

        val result = useCase().first()

        assertFalse(result.hasData)
        assertEquals(BmiMissingReason.MISSING_HEIGHT, result.missingReason)
    }

    @Test
    fun missingWeightReturnsMissingWeight() = runBlocking {
        val useCase = ObserveBmiSummaryUseCase(
            userProfileRepository = BmiFakeUserProfileRepository(profile(heightCm = 185f)),
            weightRepository = BmiFakeWeightRepository(null),
        )

        val result = useCase().first()

        assertFalse(result.hasData)
        assertEquals(BmiMissingReason.MISSING_WEIGHT, result.missingReason)
    }

    @Test
    fun changingLatestWeightEmitsUpdatedBmi() = runBlocking {
        val weightRepository = BmiFakeWeightRepository(weight(weightKg = 82.4f))
        val useCase = ObserveBmiSummaryUseCase(
            userProfileRepository = BmiFakeUserProfileRepository(profile(heightCm = 185f)),
            weightRepository = weightRepository,
        )
        val emissions = mutableListOf<com.saglik.domain.bmi.BmiSummary>()
        val firstEmissionSeen = CompletableDeferred<Unit>()

        val job = launch {
            useCase().take(2).collect { summary ->
                emissions += summary
                if (emissions.size == 1) {
                    firstEmissionSeen.complete(Unit)
                }
            }
        }

        firstEmissionSeen.await()
        weightRepository.emit(weight(weightKg = 90f))
        job.join()

        assertEquals(2, emissions.size)
        assertEquals(24.1f, emissions[0].bmi?.value ?: 0f, 0.05f)
        assertEquals(26.3f, emissions[1].bmi?.value ?: 0f, 0.05f)
        assertEquals(BmiCategory.HIGH, emissions[1].bmi?.category)
    }

    private fun profile(heightCm: Float): UserProfile =
        UserProfile(
            id = "profile",
            sex = Sex.UNSPECIFIED,
            age = 32,
            birthDate = null,
            heightCm = heightCm,
            goal = HealthGoal.GENERAL_HEALTH,
            createdAt = Instant.fromEpochMilliseconds(1),
            updatedAt = Instant.fromEpochMilliseconds(1),
        )

    private fun weight(weightKg: Float): WeightEntry =
        WeightEntry(
            id = "weight-$weightKg",
            weightKg = weightKg,
            recordedAt = Instant.fromEpochMilliseconds(1),
            source = DataSource.MANUAL,
            note = null,
        )
}

private class BmiFakeUserProfileRepository(
    initialProfile: UserProfile?,
) : UserProfileRepository {
    private val profile = MutableStateFlow(initialProfile)

    override fun observeProfile(): Flow<UserProfile?> = profile

    override suspend fun saveProfile(profile: UserProfile) {
        this.profile.value = profile
    }
}

private class BmiFakeWeightRepository(
    initialEntry: WeightEntry?,
) : WeightRepository {
    private val latestEntry = MutableStateFlow(initialEntry)
    private val entries = MutableStateFlow(initialEntry?.let(::listOf).orEmpty())

    override fun observeLatestWeightEntry(): Flow<WeightEntry?> = latestEntry

    override fun observeWeightEntries(): Flow<List<WeightEntry>> = entries

    override suspend fun addWeightEntry(entry: WeightEntry) {
        latestEntry.value = entry
        entries.value = listOf(entry)
    }

    fun emit(entry: WeightEntry?) {
        latestEntry.value = entry
        entries.value = entry?.let(::listOf).orEmpty()
    }
}
