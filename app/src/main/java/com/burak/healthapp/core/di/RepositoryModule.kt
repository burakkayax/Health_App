package com.burak.healthapp.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.CaffeineDao
import com.burak.healthapp.data.local.dao.ExerciseDao
import com.burak.healthapp.data.local.dao.HydrationDao
import com.burak.healthapp.data.local.dao.MealDao
import com.burak.healthapp.data.local.dao.SleepDao
import com.burak.healthapp.data.local.dao.SmokingDao
import com.burak.healthapp.data.local.dao.StepDao
import com.burak.healthapp.data.local.dao.SupplementDoseDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.repository.DashboardRepositoryImpl
import com.burak.healthapp.data.repository.SettingsRepositoryImpl
import com.burak.healthapp.data.repository.TrendsRepositoryImpl
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.repository.TrendsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>,
        templateDao: SupplementTemplateDao,
        measurementDao: BodyMeasurementDao,
    ): SettingsRepository = SettingsRepositoryImpl(
        dataStore = dataStore,
        templateDao = templateDao,
        measurementDao = measurementDao,
    )

    @Provides
    @Singleton
    fun provideDashboardRepository(
        settingsRepository: SettingsRepository,
        mealDao: MealDao,
        hydrationDao: HydrationDao,
        sleepDao: SleepDao,
        exerciseDao: ExerciseDao,
        smokingDao: SmokingDao,
        stepDao: StepDao,
        caffeineDao: CaffeineDao,
        templateDao: SupplementTemplateDao,
        doseDao: SupplementDoseDao,
        measurementDao: BodyMeasurementDao,
    ): DashboardRepository = DashboardRepositoryImpl(
        settingsRepository = settingsRepository,
        mealDao = mealDao,
        hydrationDao = hydrationDao,
        sleepDao = sleepDao,
        exerciseDao = exerciseDao,
        smokingDao = smokingDao,
        stepDao = stepDao,
        caffeineDao = caffeineDao,
        templateDao = templateDao,
        doseDao = doseDao,
        measurementDao = measurementDao,
    )

    @Provides
    @Singleton
    fun provideTrendsRepository(
        settingsRepository: SettingsRepository,
        mealDao: MealDao,
        hydrationDao: HydrationDao,
        sleepDao: SleepDao,
        stepDao: StepDao,
        caffeineDao: CaffeineDao,
        smokingDao: SmokingDao,
        exerciseDao: ExerciseDao,
        measurementDao: BodyMeasurementDao,
    ): TrendsRepository = TrendsRepositoryImpl(
        settingsRepository = settingsRepository,
        mealDao = mealDao,
        hydrationDao = hydrationDao,
        sleepDao = sleepDao,
        stepDao = stepDao,
        caffeineDao = caffeineDao,
        smokingDao = smokingDao,
        exerciseDao = exerciseDao,
        measurementDao = measurementDao,
    )
}
