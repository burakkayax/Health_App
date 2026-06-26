@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.app.di

import com.saglik.domain.repository.AppPreferencesRepository
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import com.saglik.domain.usecase.AddSleepEntryUseCase
import com.saglik.domain.usecase.AddWeightEntryUseCase
import com.saglik.domain.usecase.CompleteOnboardingUseCase
import com.saglik.domain.usecase.CreateInitialWeightEntryUseCase
import com.saglik.domain.usecase.ObserveBmiSummaryUseCase
import com.saglik.domain.usecase.ObserveLatestWeightEntryUseCase
import com.saglik.domain.usecase.ObserveOnboardingCompletedUseCase
import com.saglik.domain.usecase.ObserveSleepDetailUseCase
import com.saglik.domain.usecase.ObserveSleepSummaryUseCase
import com.saglik.domain.usecase.ObserveUserProfileUseCase
import com.saglik.domain.usecase.ObserveWeightTrendSummaryUseCase
import com.saglik.domain.usecase.SaveUserProfileUseCase
import com.saglik.domain.usecase.SetOnboardingCompletedUseCase
import com.saglik.domain.usecase.ValidateSleepInputUseCase
import com.saglik.domain.usecase.ResolveSleepTimeRangeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideObserveOnboardingCompletedUseCase(
        repository: AppPreferencesRepository,
    ): ObserveOnboardingCompletedUseCase = ObserveOnboardingCompletedUseCase(repository)

    @Provides
    fun provideSetOnboardingCompletedUseCase(
        repository: AppPreferencesRepository,
    ): SetOnboardingCompletedUseCase = SetOnboardingCompletedUseCase(repository)

    @Provides
    fun provideObserveUserProfileUseCase(
        repository: UserProfileRepository,
    ): ObserveUserProfileUseCase = ObserveUserProfileUseCase(repository)

    @Provides
    fun provideSaveUserProfileUseCase(
        repository: UserProfileRepository,
    ): SaveUserProfileUseCase = SaveUserProfileUseCase(repository)

    @Provides
    fun provideCreateInitialWeightEntryUseCase(
        repository: WeightRepository,
    ): CreateInitialWeightEntryUseCase = CreateInitialWeightEntryUseCase(repository)

    @Provides
    fun provideObserveLatestWeightEntryUseCase(
        repository: WeightRepository,
    ): ObserveLatestWeightEntryUseCase = ObserveLatestWeightEntryUseCase(repository)

    @Provides
    fun provideObserveWeightTrendSummaryUseCase(
        repository: WeightRepository,
    ): ObserveWeightTrendSummaryUseCase = ObserveWeightTrendSummaryUseCase(repository)

    @Provides
    fun provideAddWeightEntryUseCase(
        repository: WeightRepository,
    ): AddWeightEntryUseCase = AddWeightEntryUseCase(repository)

    @Provides
    fun provideValidateSleepInputUseCase(): ValidateSleepInputUseCase =
        ValidateSleepInputUseCase()

    @Provides
    fun provideResolveSleepTimeRangeUseCase(): ResolveSleepTimeRangeUseCase =
        ResolveSleepTimeRangeUseCase()


    @Provides
    fun provideAddSleepEntryUseCase(
        repository: SleepRepository,
    ): AddSleepEntryUseCase = AddSleepEntryUseCase(repository)

    @Provides
    fun provideObserveSleepSummaryUseCase(
        repository: SleepRepository,
    ): ObserveSleepSummaryUseCase = ObserveSleepSummaryUseCase(repository)

    @Provides
    fun provideObserveSleepDetailUseCase(
        repository: SleepRepository,
    ): ObserveSleepDetailUseCase = ObserveSleepDetailUseCase(repository)

    @Provides
    fun provideObserveBmiSummaryUseCase(
        userProfileRepository: UserProfileRepository,
        weightRepository: WeightRepository,
    ): ObserveBmiSummaryUseCase = ObserveBmiSummaryUseCase(
        userProfileRepository = userProfileRepository,
        weightRepository = weightRepository,
    )

    @Provides
    fun provideCompleteOnboardingUseCase(
        userProfileRepository: UserProfileRepository,
        weightRepository: WeightRepository,
        appPreferencesRepository: AppPreferencesRepository,
    ): CompleteOnboardingUseCase = CompleteOnboardingUseCase(
        userProfileRepository = userProfileRepository,
        weightRepository = weightRepository,
        appPreferencesRepository = appPreferencesRepository,
    )
}
