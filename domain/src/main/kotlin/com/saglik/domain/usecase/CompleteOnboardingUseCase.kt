@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.UserProfile
import com.saglik.core.model.WeightEntry
import com.saglik.domain.onboarding.CompleteOnboardingInput
import com.saglik.domain.onboarding.OnboardingValidationErrors
import com.saglik.domain.onboarding.OnboardingValidator
import com.saglik.domain.repository.AppPreferencesRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import java.util.UUID
import kotlinx.datetime.Instant

class CompleteOnboardingUseCase(
    private val userProfileRepository: UserProfileRepository,
    private val weightRepository: WeightRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val nowProvider: () -> Instant = { Instant.fromEpochMilliseconds(System.currentTimeMillis()) },
    private val idProvider: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(input: CompleteOnboardingInput): CompleteOnboardingResult {
        val validation = OnboardingValidator.validateForCompletion(input)
        if (!validation.isValid) {
            return CompleteOnboardingResult.ValidationError(validation)
        }

        val now = nowProvider()
        val profile = UserProfile(
            id = USER_PROFILE_ID,
            sex = requireNotNull(input.sex),
            age = requireNotNull(input.age),
            birthDate = null,
            heightCm = requireNotNull(input.heightCm),
            goal = requireNotNull(input.goal),
            createdAt = now,
            updatedAt = now,
        )
        val weightEntry = WeightEntry(
            id = idProvider(),
            weightKg = requireNotNull(input.startingWeightKg),
            recordedAt = now,
            source = DataSource.MANUAL,
            note = null,
        )

        userProfileRepository.saveProfile(profile)
        weightRepository.addWeightEntry(weightEntry)
        appPreferencesRepository.setOnboardingCompleted(true)

        return CompleteOnboardingResult.Success
    }

    companion object {
        const val USER_PROFILE_ID = "primary"
    }
}

sealed interface CompleteOnboardingResult {
    data object Success : CompleteOnboardingResult

    data class ValidationError(
        val errors: OnboardingValidationErrors,
    ) : CompleteOnboardingResult
}
