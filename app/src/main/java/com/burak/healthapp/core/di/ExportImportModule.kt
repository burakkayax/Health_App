package com.burak.healthapp.core.di

import android.content.Context
import com.burak.healthapp.BuildConfig
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.data.export.AndroidHealthDataExportFileWriter
import com.burak.healthapp.data.export.AndroidHealthDataImportFileReader
import com.burak.healthapp.data.export.HealthDataExportFileWriter
import com.burak.healthapp.data.export.HealthDataExportRepositoryImpl
import com.burak.healthapp.data.export.HealthDataImportFileReader
import com.burak.healthapp.data.export.HealthDataManagementRepositoryImpl
import com.burak.healthapp.data.export.JsonHealthDataExporter
import com.burak.healthapp.data.export.JsonHealthDataImporter
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
import com.burak.healthapp.domain.export.HealthDataJsonExporter
import com.burak.healthapp.domain.export.HealthDataJsonImporter
import com.burak.healthapp.domain.repository.HealthDataExportRepository
import com.burak.healthapp.domain.repository.HealthDataManagementRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.usecase.DeleteAllHealthDataUseCase
import com.burak.healthapp.domain.usecase.ExportHealthDataUseCase
import com.burak.healthapp.domain.usecase.ImportHealthDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExportImportModule {
    @Provides
    @Singleton
    fun provideHealthDataExportRepository(
        settingsRepository: SettingsRepository,
        mealDao: MealDao,
        hydrationDao: HydrationDao,
        sleepDao: SleepDao,
        exerciseDao: ExerciseDao,
        smokingDao: SmokingDao,
        stepDao: StepDao,
        caffeineDao: CaffeineDao,
        measurementDao: BodyMeasurementDao,
        templateDao: SupplementTemplateDao,
        doseDao: SupplementDoseDao,
    ): HealthDataExportRepository = HealthDataExportRepositoryImpl(
        settingsRepository = settingsRepository,
        mealDao = mealDao,
        hydrationDao = hydrationDao,
        sleepDao = sleepDao,
        exerciseDao = exerciseDao,
        smokingDao = smokingDao,
        stepDao = stepDao,
        caffeineDao = caffeineDao,
        measurementDao = measurementDao,
        templateDao = templateDao,
        doseDao = doseDao,
    )

    @Provides
    @Singleton
    fun provideHealthDataManagementRepository(
        database: HealthDatabase,
        settingsRepository: SettingsRepository,
    ): HealthDataManagementRepository = HealthDataManagementRepositoryImpl(
        database = database,
        settingsRepository = settingsRepository,
    )

    @Provides
    fun provideJsonHealthDataExporter(): HealthDataJsonExporter = JsonHealthDataExporter()

    @Provides
    fun provideJsonHealthDataImporter(): HealthDataJsonImporter = JsonHealthDataImporter()

    @Provides
    fun provideExportHealthDataUseCase(
        repository: HealthDataExportRepository,
        jsonExporter: HealthDataJsonExporter,
    ): ExportHealthDataUseCase = ExportHealthDataUseCase(
        repository = repository,
        jsonExporter = jsonExporter,
        appVersion = BuildConfig.VERSION_NAME,
    )

    @Provides
    fun provideImportHealthDataUseCase(
        repository: HealthDataManagementRepository,
    ): ImportHealthDataUseCase = ImportHealthDataUseCase(repository)

    @Provides
    fun provideDeleteAllHealthDataUseCase(
        repository: HealthDataManagementRepository,
    ): DeleteAllHealthDataUseCase = DeleteAllHealthDataUseCase(repository)

    @Provides
    fun provideHealthDataExportFileWriter(
        @ApplicationContext context: Context,
    ): HealthDataExportFileWriter = AndroidHealthDataExportFileWriter(context)

    @Provides
    fun provideHealthDataImportFileReader(
        @ApplicationContext context: Context,
    ): HealthDataImportFileReader = AndroidHealthDataImportFileReader(context)
}
