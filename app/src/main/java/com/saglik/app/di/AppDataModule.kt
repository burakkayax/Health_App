@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.saglik.core.database.AppDatabase
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.UserProfileDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.datastore.AppPreferencesDataSource
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.healthconnect.NoOpHealthConnectDataSource
import com.saglik.data.local.PreferencesLocalDataSource
import com.saglik.data.repository.DefaultAppPreferencesRepository
import com.saglik.data.repository.DefaultSleepRepository
import com.saglik.data.repository.DefaultUserProfileRepository
import com.saglik.data.repository.DefaultWeightRepository
import com.saglik.domain.repository.AppPreferencesRepository
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import com.saglik.domain.usecase.AddSleepEntryUseCase
import com.saglik.domain.usecase.AddWeightEntryUseCase
import com.saglik.domain.usecase.CompleteOnboardingUseCase
import com.saglik.domain.usecase.CreateInitialWeightEntryUseCase
import com.saglik.domain.usecase.ObserveBmiSummaryUseCase
import com.saglik.domain.usecase.ObserveSleepDetailUseCase
import com.saglik.domain.usecase.ObserveSleepSummaryUseCase
import com.saglik.domain.usecase.ObserveLatestWeightEntryUseCase
import com.saglik.domain.usecase.ObserveOnboardingCompletedUseCase
import com.saglik.domain.usecase.ObserveUserProfileUseCase
import com.saglik.domain.usecase.ObserveWeightTrendSummaryUseCase
import com.saglik.domain.usecase.SaveUserProfileUseCase
import com.saglik.domain.usecase.SetOnboardingCompletedUseCase
import com.saglik.domain.usecase.ValidateSleepInputUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

@Module
@InstallIn(SingletonComponent::class)
object AppDataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "health_app.db",
    )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .build()

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideWeightDao(database: AppDatabase): WeightDao = database.weightDao()

    @Provides
    fun provideSleepDao(database: AppDatabase): SleepDao = database.sleepDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.appPreferencesDataStore

    @Provides
    @Singleton
    fun provideAppPreferencesDataSource(
        dataStore: DataStore<Preferences>,
    ): AppPreferencesDataSource = AppPreferencesDataSource(dataStore)

    @Provides
    @Singleton
    fun providePreferencesLocalDataSource(
        preferencesDataSource: AppPreferencesDataSource,
    ): PreferencesLocalDataSource = PreferencesLocalDataSource(preferencesDataSource)

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(
        localDataSource: PreferencesLocalDataSource,
    ): AppPreferencesRepository = DefaultAppPreferencesRepository(localDataSource)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        userProfileDao: UserProfileDao,
    ): UserProfileRepository = DefaultUserProfileRepository(userProfileDao)

    @Provides
    @Singleton
    fun provideWeightRepository(
        weightDao: WeightDao,
    ): WeightRepository = DefaultWeightRepository(weightDao)

    @Provides
    @Singleton
    fun provideSleepRepository(
        sleepDao: SleepDao,
    ): SleepRepository = DefaultSleepRepository(sleepDao)

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

    @Provides
    @Singleton
    fun provideHealthConnectDataSource(): HealthConnectDataSource = NoOpHealthConnectDataSource()
}
