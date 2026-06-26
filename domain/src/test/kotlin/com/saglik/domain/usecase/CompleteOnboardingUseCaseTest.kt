@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex
import com.saglik.core.model.UserProfile
import com.saglik.core.model.WeightEntry
import com.saglik.domain.onboarding.CompleteOnboardingInput
import com.saglik.domain.repository.AppPreferencesRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteOnboardingUseCaseTest {
    @Test
    fun validInputSavesProfileCreatesManualWeightEntryAndSetsCompleted() = runBlocking {
        val userProfileRepository = FakeUserProfileRepository()
        val weightRepository = FakeWeightRepository()
        val appPreferencesRepository = FakeAppPreferencesRepository()
        val now = Instant.fromEpochMilliseconds(1_234)
        val useCase = CompleteOnboardingUseCase(
            userProfileRepository = userProfileRepository,
            weightRepository = weightRepository,
            appPreferencesRepository = appPreferencesRepository,
            nowProvider = { now },
            idProvider = { "weight-entry-id" },
        )

        val result = useCase(validInput())

        assertEquals(CompleteOnboardingResult.Success, result)
        assertEquals(1, userProfileRepository.savedProfiles.size)
        assertEquals(Sex.FEMALE, userProfileRepository.savedProfiles.single().sex)
        assertEquals(168f, userProfileRepository.savedProfiles.single().heightCm)
        assertEquals(now, userProfileRepository.savedProfiles.single().createdAt)
        assertEquals(1, weightRepository.savedEntries.size)
        assertEquals("weight-entry-id", weightRepository.savedEntries.single().id)
        assertEquals(68f, weightRepository.savedEntries.single().weightKg)
        assertEquals(DataSource.MANUAL, weightRepository.savedEntries.single().source)
        assertTrue(appPreferencesRepository.completed)
    }

    @Test
    fun invalidInputDoesNotSavePartialData() = runBlocking {
        val userProfileRepository = FakeUserProfileRepository()
        val weightRepository = FakeWeightRepository()
        val appPreferencesRepository = FakeAppPreferencesRepository()
        val useCase = CompleteOnboardingUseCase(
            userProfileRepository = userProfileRepository,
            weightRepository = weightRepository,
            appPreferencesRepository = appPreferencesRepository,
        )

        val result = useCase(validInput(age = 12))

        assertTrue(result is CompleteOnboardingResult.ValidationError)
        assertTrue(userProfileRepository.savedProfiles.isEmpty())
        assertTrue(weightRepository.savedEntries.isEmpty())
        assertEquals(false, appPreferencesRepository.completed)
    }

    private fun validInput(
        sex: Sex? = Sex.FEMALE,
        age: Int? = 35,
        heightCm: Float? = 168f,
        startingWeightKg: Float? = 68f,
        goal: HealthGoal? = HealthGoal.GENERAL_HEALTH,
    ) = CompleteOnboardingInput(
        sex = sex,
        age = age,
        heightCm = heightCm,
        startingWeightKg = startingWeightKg,
        goal = goal,
    )
}

private class FakeUserProfileRepository : UserProfileRepository {
    val savedProfiles = mutableListOf<UserProfile>()
    private val profile = MutableStateFlow<UserProfile?>(null)

    override fun observeProfile(): Flow<UserProfile?> = profile

    override suspend fun saveProfile(profile: UserProfile) {
        savedProfiles += profile
        this.profile.value = profile
    }
}

private class FakeWeightRepository : WeightRepository {
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

private class FakeAppPreferencesRepository : AppPreferencesRepository {
    var completed = false
    private val completedFlow = MutableStateFlow(false)

    override fun observeOnboardingCompleted(): Flow<Boolean> = completedFlow

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        this.completed = completed
        completedFlow.value = completed
    }
}
