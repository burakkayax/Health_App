package com.burak.healthapp.core.di

import android.content.Context
import androidx.room.Room
import com.burak.healthapp.BuildConfig
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.core.database.MIGRATION_1_2
import com.burak.healthapp.core.database.MIGRATION_2_3
import com.burak.healthapp.core.database.MIGRATION_3_4
import com.burak.healthapp.core.datastore.settingsDataStore
import com.burak.healthapp.core.reminder.WaterReminderScheduler
import com.burak.healthapp.data.export.AndroidHealthDataExportFileWriter
import com.burak.healthapp.data.export.HealthDataExportFileWriter
import com.burak.healthapp.data.export.HealthDataExportRepositoryImpl
import com.burak.healthapp.data.export.JsonHealthDataExporter
import com.burak.healthapp.data.repository.DashboardRepositoryImpl
import com.burak.healthapp.data.repository.SettingsRepositoryImpl
import com.burak.healthapp.data.repository.TrendsRepositoryImpl
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.HealthDataExportRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.repository.TrendsRepository
import com.burak.healthapp.domain.usecase.ExportHealthDataUseCase

class AppContainer(context: Context) {
    private val database: HealthDatabase = Room.databaseBuilder(
        context,
        HealthDatabase::class.java,
        "health.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(
        dataStore = context.settingsDataStore,
        templateDao = database.supplementTemplateDao(),
        measurementDao = database.bodyMeasurementDao(),
    )

    val waterReminderScheduler: WaterReminderScheduler = WaterReminderScheduler(context)

    val dashboardRepository: DashboardRepository = DashboardRepositoryImpl(
        settingsRepository = settingsRepository,
        mealDao = database.mealDao(),
        hydrationDao = database.hydrationDao(),
        sleepDao = database.sleepDao(),
        exerciseDao = database.exerciseDao(),
        smokingDao = database.smokingDao(),
        stepDao = database.stepDao(),
        templateDao = database.supplementTemplateDao(),
        doseDao = database.supplementDoseDao(),
        measurementDao = database.bodyMeasurementDao(),
    )

    val trendsRepository: TrendsRepository = TrendsRepositoryImpl(
        settingsRepository = settingsRepository,
        mealDao = database.mealDao(),
        hydrationDao = database.hydrationDao(),
        sleepDao = database.sleepDao(),
        stepDao = database.stepDao(),
        measurementDao = database.bodyMeasurementDao(),
    )

    private val healthDataExportRepository: HealthDataExportRepository = HealthDataExportRepositoryImpl(
        settingsRepository = settingsRepository,
        mealDao = database.mealDao(),
        hydrationDao = database.hydrationDao(),
        sleepDao = database.sleepDao(),
        exerciseDao = database.exerciseDao(),
        smokingDao = database.smokingDao(),
        stepDao = database.stepDao(),
        measurementDao = database.bodyMeasurementDao(),
        templateDao = database.supplementTemplateDao(),
        doseDao = database.supplementDoseDao(),
    )

    val exportHealthDataUseCase: ExportHealthDataUseCase = ExportHealthDataUseCase(
        repository = healthDataExportRepository,
        jsonExporter = JsonHealthDataExporter(),
        appVersion = BuildConfig.VERSION_NAME,
    )

    val healthDataExportFileWriter: HealthDataExportFileWriter = AndroidHealthDataExportFileWriter(context)
}
