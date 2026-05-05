package com.burak.healthapp

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.core.database.MIGRATION_4_5
import com.burak.healthapp.core.database.MIGRATION_5_6
import com.burak.healthapp.core.database.MIGRATION_6_7
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HealthDatabase::class.java,
    )

    @Test
    fun migration4To5_preservesRecordsAndAddsIndexes() {
        helper.createDatabase(TEST_DB, 4).apply {
            seedVersion4Data()
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        assertEquals(1, db.countRows("hydration_entries"))
        assertEquals(1, db.countRows("meal_entries"))
        assertEquals(1, db.countRows("body_measurements"))
        assertEquals(1, db.countRows("sleep_sessions"))
        assertTrue(db.indexesFor("hydration_entries").contains("index_hydration_entries_date_createdAt"))
        assertTrue(db.indexesFor("meal_entries").contains("index_meal_entries_date_mealType_createdAt"))
        assertTrue(db.indexesFor("body_measurements").contains("index_body_measurements_date"))
        assertTrue(db.indexesFor("sleep_sessions").contains("index_sleep_sessions_sessionDate"))
        assertTrue(db.indexesFor("exercise_entries").contains("index_exercise_entries_date"))
        assertTrue(db.indexesFor("smoking_entries").contains("index_smoking_entries_date"))
        assertTrue(db.indexesFor("step_entries").contains("index_step_entries_date"))
        assertTrue(db.indexesFor("supplement_dose_entries").contains("index_supplement_dose_entries_templateId_date"))
    }

    @Test
    fun migrate4ToLatest_preservesDatabaseOpenability() {
        helper.createDatabase(TEST_DB, 4).apply {
            seedVersion4Data()
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            7,
            true,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
        )

        assertEquals(1, db.countRows("hydration_entries"))
        assertEquals(1, db.countRows("meal_entries"))
        assertTrue(db.tableExists("caffeine_entries"))
        assertTrue(db.tableExists("custom_foods"))
    }

    @Test
    fun migrate5To6_addsCaffeineTablesOrColumns() {
        helper.createDatabase(TEST_DB, 5).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        assertTrue(db.tableExists("caffeine_entries"))
        assertTrue(db.columnsFor("caffeine_entries").containsAll(CAFFEINE_COLUMNS))
        assertTrue(db.indexesFor("caffeine_entries").contains("index_caffeine_entries_date"))
        assertTrue(db.indexesFor("caffeine_entries").contains("index_caffeine_entries_date_time"))
        assertTrue(db.indexesFor("caffeine_entries").contains("index_caffeine_entries_date_createdAt"))
    }

    @Test
    fun migrate6To7_addsCustomFoodTablesOrColumns() {
        helper.createDatabase(TEST_DB, 6).close()

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        assertTrue(db.tableExists("custom_foods"))
        assertTrue(db.columnsFor("custom_foods").containsAll(CUSTOM_FOOD_COLUMNS))
        assertTrue(db.indexesFor("custom_foods").contains("index_custom_foods_name"))
        assertTrue(db.indexesFor("custom_foods").contains("index_custom_foods_isFavorite"))
        assertTrue(db.indexesFor("custom_foods").contains("index_custom_foods_updatedAt"))
    }

    @Test
    fun migrateEachStepToLatest_opensSuccessfully() {
        helper.createDatabase(TEST_DB, 5).close()
        assertTrue(
            helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
                .tableExists("caffeine_entries"),
        )

        helper.createDatabase(TEST_DB, 6).close()
        assertTrue(
            helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)
                .tableExists("custom_foods"),
        )
    }

    private fun SupportSQLiteDatabase.seedVersion4Data() {
        execSQL("INSERT INTO hydration_entries (id, date, amountMl, createdAt) VALUES (1, '2026-04-27', 500, '2026-04-27T09:00')")
        execSQL(
            """
            INSERT INTO meal_entries (
                id, date, mealType, name, calories, carbsGrams, fatGrams, proteinGrams, createdAt
            ) VALUES (
                1, '2026-04-27', 'BREAKFAST', 'Yumurta', 250, 2, 15, 20, '2026-04-27T08:00'
            )
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO body_measurements (
                id, date, weightKg, shoulderCm, waistCm, hipCm, recordedAt
            ) VALUES (
                1, '2026-04-27', 78.0, 118.0, 88.0, 99.0, '2026-04-27T08:00'
            )
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO body_measurements (
                id, date, weightKg, shoulderCm, waistCm, hipCm, recordedAt
            ) VALUES (
                2, '2026-04-27', 77.5, 118.0, 87.0, 99.0, '2026-04-27T09:00'
            )
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO sleep_sessions (
                id, sessionDate, startTime, endTime
            ) VALUES (
                1, '2026-04-27', '2026-04-26T23:00', '2026-04-27T07:00'
            )
            """.trimIndent(),
        )
        execSQL("INSERT INTO exercise_entries (id, date, type, durationMinutes, intensity) VALUES (1, '2026-04-27', 'CARDIO', 45, 'MEDIUM')")
        execSQL("INSERT INTO smoking_entries (id, date, count) VALUES (1, '2026-04-27', 0)")
        execSQL("INSERT INTO step_entries (id, date, steps, sensorBaseline, lastSensorValue, updatedAt) VALUES (1, '2026-04-27', 8000, NULL, NULL, '2026-04-27T20:00')")
        execSQL(
            """
            INSERT INTO supplement_templates (
                id, name, targetAmount, unitLabel, isActive, sortOrder
            ) VALUES (
                1, 'D3', 25.0, 'mcg', 1, 0
            )
            """.trimIndent(),
        )
        execSQL(
            """
            INSERT INTO supplement_dose_entries (
                id, templateId, date, amount, loggedAt
            ) VALUES (
                1, 1, '2026-04-27', 25.0, '2026-04-27T09:00'
            )
            """.trimIndent(),
        )
    }

    private fun SupportSQLiteDatabase.countRows(table: String): Int = query("SELECT COUNT(*) FROM $table").use { cursor ->
        cursor.moveToFirst()
        cursor.getInt(0)
    }

    private fun SupportSQLiteDatabase.indexesFor(table: String): Set<String> = query("PRAGMA index_list(`$table`)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                add(cursor.getString(nameIndex))
            }
        }
    }

    private fun SupportSQLiteDatabase.columnsFor(table: String): Set<String> = query("PRAGMA table_info(`$table`)").use { cursor ->
        buildSet {
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                add(cursor.getString(nameIndex))
            }
        }
    }

    private fun SupportSQLiteDatabase.tableExists(table: String): Boolean = query(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        arrayOf(table),
    ).use { cursor ->
        cursor.moveToFirst()
    }

    private companion object {
        const val TEST_DB = "health-migration-test"
        val CAFFEINE_COLUMNS = setOf(
            "id",
            "date",
            "time",
            "drinkType",
            "size",
            "estimatedMg",
            "customName",
            "createdAt",
        )
        val CUSTOM_FOOD_COLUMNS = setOf(
            "id",
            "name",
            "brand",
            "servingName",
            "servingGrams",
            "calories",
            "proteinGrams",
            "carbsGrams",
            "fatGrams",
            "fiberGrams",
            "sugarGrams",
            "sodiumMg",
            "isFavorite",
            "createdAt",
            "updatedAt",
        )
    }
}
