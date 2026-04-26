package com.burak.healthapp

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.burak.healthapp.data.local.HealthDatabase
import com.burak.healthapp.data.repository.DashboardRepository
import com.burak.healthapp.data.repository.DefaultDashboardRepository
import com.burak.healthapp.data.repository.DefaultSettingsRepository
import com.burak.healthapp.data.repository.DefaultTrendsRepository
import com.burak.healthapp.data.repository.SettingsRepository
import com.burak.healthapp.data.repository.TrendsRepository
import com.burak.healthapp.notifications.HealthNotifications
import com.burak.healthapp.reminders.WaterReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore(name = "health_preferences")

class HealthApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: HealthAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = HealthAppContainer(applicationContext)
        HealthNotifications.ensureChannels(applicationContext)
        applicationScope.launch {
            container.waterReminderScheduler.apply(
                container.settingsRepository.settings.first().waterReminderSettings,
            )
        }
    }
}

class HealthAppContainer(context: Context) {
    private val database: HealthDatabase = Room.databaseBuilder(
        context,
        HealthDatabase::class.java,
        "health.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    val settingsRepository: SettingsRepository = DefaultSettingsRepository(
        dataStore = context.settingsDataStore,
        templateDao = database.supplementTemplateDao(),
        measurementDao = database.bodyMeasurementDao(),
    )

    val waterReminderScheduler: WaterReminderScheduler = WaterReminderScheduler(context)

    val dashboardRepository: DashboardRepository = DefaultDashboardRepository(
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

    val trendsRepository: TrendsRepository = DefaultTrendsRepository(
        settingsRepository = settingsRepository,
        mealDao = database.mealDao(),
        hydrationDao = database.hydrationDao(),
        sleepDao = database.sleepDao(),
        stepDao = database.stepDao(),
        measurementDao = database.bodyMeasurementDao(),
    )
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE supplement_templates
            ADD COLUMN targetAmount REAL NOT NULL DEFAULT 1.0
            """.trimIndent(),
        )
        db.execSQL(
            """
            ALTER TABLE supplement_templates
            ADD COLUMN unitLabel TEXT NOT NULL DEFAULT 'doz'
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS supplement_dose_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                templateId INTEGER NOT NULL,
                date TEXT NOT NULL,
                amount REAL NOT NULL,
                loggedAt TEXT NOT NULL,
                FOREIGN KEY(templateId) REFERENCES supplement_templates(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_supplement_dose_entries_templateId_date
            ON supplement_dose_entries (templateId, date)
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE supplement_templates SET targetAmount = 500, unitLabel = 'mcg'
            WHERE lower(name) = 'b12'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE supplement_templates SET targetAmount = 25, unitLabel = 'mcg'
            WHERE lower(name) = 'd3 vitamini'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE supplement_templates SET targetAmount = 18, unitLabel = 'mg'
            WHERE lower(name) = 'demir'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE supplement_templates SET targetAmount = 1000, unitLabel = 'mg'
            WHERE lower(name) = 'omega 3'
            """.trimIndent(),
        )
        db.execSQL(
            """
            UPDATE supplement_templates SET targetAmount = 200, unitLabel = 'mg'
            WHERE lower(name) = 'magnezyum'
            """.trimIndent(),
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exercise_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                durationMinutes INTEGER NOT NULL,
                intensity TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_entries_date
            ON exercise_entries (date)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS smoking_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                count INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_smoking_entries_date
            ON smoking_entries (date)
            """.trimIndent(),
        )
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS step_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                steps INTEGER NOT NULL,
                sensorBaseline INTEGER,
                lastSensorValue INTEGER,
                updatedAt TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_step_entries_date
            ON step_entries (date)
            """.trimIndent(),
        )
    }
}
