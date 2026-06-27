package com.saglik.core.database

import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate3To4_preservesWeightEntriesAndBackfillsMetadata() {
        helper.createDatabase(WEIGHT_TEST_DB, 3).apply {
            execSQL(
                """
                INSERT INTO weight_entries(id, weightKg, recordedAt, source, note)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>("weight-1", 82.5, 1_720_000_000_000L, "MANUAL", "morning"),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            WEIGHT_TEST_DB,
            4,
            true,
            AppDatabase.MIGRATION_3_4,
        )

        db.query("SELECT * FROM weight_entries WHERE id = 'weight-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("weight-1", cursor.string("id"))
            assertEquals(82.5f, cursor.getFloat(cursor.getColumnIndexOrThrow("weightKg")), 0.001f)
            assertEquals(1_720_000_000_000L, cursor.long("recordedAt"))
            assertEquals("MANUAL", cursor.string("source"))
            assertEquals("morning", cursor.string("note"))
            cursor.assertNullColumn("sourceRecordId")
            cursor.assertNullColumn("sourcePackageName")
            cursor.assertNullColumn("sourceAppName")
            cursor.assertNullColumn("lastSyncedAt")
            cursor.assertNullColumn("deletedAt")
            assertEquals(1_720_000_000_000L, cursor.long("createdAt"))
            assertEquals(1_720_000_000_000L, cursor.long("updatedAt"))
        }
        db.close()
    }

    @Test
    fun migrate3To4_preservesSleepEntriesAndBackfillsMetadata() {
        helper.createDatabase(SLEEP_TEST_DB, 3).apply {
            execSQL(
                """
                INSERT INTO sleep_entries(id, startTime, endTime, durationMinutes, quality, source, note)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    "sleep-1",
                    1_720_036_000_000L,
                    1_720_064_800_000L,
                    480,
                    "GOOD",
                    "MANUAL",
                    "rested",
                ),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            SLEEP_TEST_DB,
            4,
            true,
            AppDatabase.MIGRATION_3_4,
        )

        db.query("SELECT * FROM sleep_entries WHERE id = 'sleep-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("sleep-1", cursor.string("id"))
            assertEquals(1_720_036_000_000L, cursor.long("startTime"))
            assertEquals(1_720_064_800_000L, cursor.long("endTime"))
            assertEquals(480, cursor.getInt(cursor.getColumnIndexOrThrow("durationMinutes")))
            assertEquals("GOOD", cursor.string("quality"))
            assertEquals("MANUAL", cursor.string("source"))
            assertEquals("rested", cursor.string("note"))
            cursor.assertNullColumn("sourceRecordId")
            cursor.assertNullColumn("sourcePackageName")
            cursor.assertNullColumn("sourceAppName")
            cursor.assertNullColumn("lastSyncedAt")
            cursor.assertNullColumn("deletedAt")
            assertEquals(1_720_064_800_000L, cursor.long("createdAt"))
            assertEquals(1_720_064_800_000L, cursor.long("updatedAt"))
        }
        db.close()
    }

    @Test
    fun migrate3To4_allowsManualDuplicatesAndRejectsDuplicateExternalIdentity() {
        helper.createDatabase(INDEX_TEST_DB, 3).close()

        val db = helper.runMigrationsAndValidate(
            INDEX_TEST_DB,
            4,
            true,
            AppDatabase.MIGRATION_3_4,
        )

        db.execSQL(
            """
            INSERT INTO weight_entries(id, weightKg, recordedAt, source, note, createdAt, updatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>("manual-weight-1", 80.0, 1_720_000_000_000L, "MANUAL", null, 1L, 1L),
        )
        db.execSQL(
            """
            INSERT INTO weight_entries(id, weightKg, recordedAt, source, note, createdAt, updatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>("manual-weight-2", 80.0, 1_720_000_000_000L, "MANUAL", null, 1L, 1L),
        )

        db.execSQL(
            """
            INSERT INTO sleep_entries(
                id,
                startTime,
                endTime,
                durationMinutes,
                quality,
                source,
                note,
                createdAt,
                updatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "manual-sleep-1",
                1_720_036_000_000L,
                1_720_064_800_000L,
                480,
                null,
                "MANUAL",
                null,
                1L,
                1L,
            ),
        )
        db.execSQL(
            """
            INSERT INTO sleep_entries(
                id,
                startTime,
                endTime,
                durationMinutes,
                quality,
                source,
                note,
                createdAt,
                updatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "manual-sleep-2",
                1_720_036_000_000L,
                1_720_064_800_000L,
                480,
                null,
                "MANUAL",
                null,
                1L,
                1L,
            ),
        )

        insertExternalWeight(db, "external-weight-1")
        expectUniqueConstraintFailure {
            insertExternalWeight(db, "external-weight-2")
        }

        insertExternalSleep(db, "external-sleep-1")
        expectUniqueConstraintFailure {
            insertExternalSleep(db, "external-sleep-2")
        }
        db.close()
    }

    @Test
    fun migrate4To5_createsStepsTableAndRejectsDuplicateExternalIdentity() {
        helper.createDatabase(STEPS_TEST_DB, 4).close()

        val db = helper.runMigrationsAndValidate(
            STEPS_TEST_DB,
            5,
            true,
            AppDatabase.MIGRATION_4_5,
        )

        db.execSQL(
            """
            INSERT INTO steps_entries(
                id,
                startTime,
                endTime,
                count,
                source,
                note,
                createdAt,
                updatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "manual-steps-1",
                1_720_036_000_000L,
                1_720_039_600_000L,
                1_200L,
                "MANUAL",
                null,
                1L,
                1L,
            ),
        )
        db.execSQL(
            """
            INSERT INTO steps_entries(
                id,
                startTime,
                endTime,
                count,
                source,
                note,
                createdAt,
                updatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "manual-steps-2",
                1_720_036_000_000L,
                1_720_039_600_000L,
                1_200L,
                "MANUAL",
                null,
                1L,
                1L,
            ),
        )

        insertExternalSteps(db, "external-steps-1")
        expectUniqueConstraintFailure {
            insertExternalSteps(db, "external-steps-2")
        }

        db.query("SELECT * FROM steps_entries WHERE id = 'external-steps-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("external-steps-1", cursor.string("id"))
            assertEquals(1_720_036_000_000L, cursor.long("startTime"))
            assertEquals(1_720_039_600_000L, cursor.long("endTime"))
            assertEquals(1_200L, cursor.long("count"))
            assertEquals("HEALTH_CONNECT", cursor.string("source"))
            assertEquals("external-steps-record", cursor.string("sourceRecordId"))
            assertEquals("com.example.health", cursor.string("sourcePackageName"))
        }
        db.close()
    }

    @Test
    fun migrate5To6_createsExerciseSessionsTableAndRejectsDuplicateExternalIdentity() {
        helper.createDatabase(EXERCISE_TEST_DB, 5).close()

        val db = helper.runMigrationsAndValidate(
            EXERCISE_TEST_DB,
            6,
            true,
            AppDatabase.MIGRATION_5_6,
        )

        db.execSQL(
            """
            INSERT INTO exercise_sessions(
                id,
                startTime,
                endTime,
                durationMinutes,
                exerciseType,
                title,
                notes,
                source,
                createdAt,
                updatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "manual-exercise-1",
                1_720_036_000_000L,
                1_720_039_600_000L,
                60,
                "WALKING",
                "Walk",
                null,
                "MANUAL",
                1L,
                1L,
            ),
        )
        db.execSQL(
            """
            INSERT INTO exercise_sessions(
                id,
                startTime,
                endTime,
                durationMinutes,
                exerciseType,
                title,
                notes,
                source,
                createdAt,
                updatedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                "manual-exercise-2",
                1_720_036_000_000L,
                1_720_039_600_000L,
                60,
                "WALKING",
                "Walk",
                null,
                "MANUAL",
                1L,
                1L,
            ),
        )

        insertExternalExercise(db, "external-exercise-1")
        expectUniqueConstraintFailure {
            insertExternalExercise(db, "external-exercise-2")
        }

        db.query("SELECT * FROM exercise_sessions WHERE id = 'external-exercise-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("external-exercise-1", cursor.string("id"))
            assertEquals(1_720_036_000_000L, cursor.long("startTime"))
            assertEquals(1_720_039_600_000L, cursor.long("endTime"))
            assertEquals(60, cursor.getInt(cursor.getColumnIndexOrThrow("durationMinutes")))
            assertEquals("WALKING", cursor.string("exerciseType"))
            assertEquals("HEALTH_CONNECT", cursor.string("source"))
            assertEquals("external-exercise-record", cursor.string("sourceRecordId"))
            assertEquals("com.example.health", cursor.string("sourcePackageName"))
        }
        db.close()
    }

    private fun insertExternalWeight(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        id: String,
    ) {
        db.execSQL(
            """
            INSERT INTO weight_entries(
                id,
                weightKg,
                recordedAt,
                source,
                note,
                sourceRecordId,
                sourcePackageName,
                sourceAppName,
                createdAt,
                updatedAt,
                lastSyncedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                id,
                81.0,
                1_720_000_000_000L,
                "HEALTH_CONNECT",
                null,
                "external-weight-record",
                "com.example.health",
                "Example Health",
                1L,
                1L,
                1L,
            ),
        )
    }

    private fun insertExternalSleep(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        id: String,
    ) {
        db.execSQL(
            """
            INSERT INTO sleep_entries(
                id,
                startTime,
                endTime,
                durationMinutes,
                quality,
                source,
                note,
                sourceRecordId,
                sourcePackageName,
                sourceAppName,
                createdAt,
                updatedAt,
                lastSyncedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                id,
                1_720_036_000_000L,
                1_720_064_800_000L,
                480,
                "GOOD",
                "HEALTH_CONNECT",
                null,
                "external-sleep-record",
                "com.example.health",
                "Example Health",
                1L,
                1L,
                1L,
            ),
        )
    }

    private fun insertExternalSteps(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        id: String,
    ) {
        db.execSQL(
            """
            INSERT INTO steps_entries(
                id,
                startTime,
                endTime,
                count,
                source,
                note,
                sourceRecordId,
                sourcePackageName,
                sourceAppName,
                createdAt,
                updatedAt,
                lastSyncedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                id,
                1_720_036_000_000L,
                1_720_039_600_000L,
                1_200L,
                "HEALTH_CONNECT",
                null,
                "external-steps-record",
                "com.example.health",
                "Example Health",
                1L,
                1L,
                1L,
            ),
        )
    }

    private fun insertExternalExercise(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        id: String,
    ) {
        db.execSQL(
            """
            INSERT INTO exercise_sessions(
                id,
                startTime,
                endTime,
                durationMinutes,
                exerciseType,
                title,
                notes,
                source,
                sourceRecordId,
                sourcePackageName,
                sourceAppName,
                createdAt,
                updatedAt,
                lastSyncedAt
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                id,
                1_720_036_000_000L,
                1_720_039_600_000L,
                60,
                "WALKING",
                "Walk",
                null,
                "HEALTH_CONNECT",
                "external-exercise-record",
                "com.example.health",
                "Example Health",
                1L,
                1L,
                1L,
            ),
        )
    }

    private fun expectUniqueConstraintFailure(block: () -> Unit) {
        try {
            block()
            fail("Expected duplicate external identity to fail.")
        } catch (_: SQLiteConstraintException) {
            // Expected duplicate rejection from the unique external identity index.
        }
    }

    private fun Cursor.string(column: String): String? =
        getString(getColumnIndexOrThrow(column))

    private fun Cursor.long(column: String): Long =
        getLong(getColumnIndexOrThrow(column))

    private fun Cursor.assertNullColumn(column: String) {
        assertTrue("$column should be null", isNull(getColumnIndexOrThrow(column)))
    }

    private companion object {
        const val WEIGHT_TEST_DB = "weight-migration-test.db"
        const val SLEEP_TEST_DB = "sleep-migration-test.db"
        const val INDEX_TEST_DB = "index-migration-test.db"
        const val STEPS_TEST_DB = "steps-migration-test.db"
        const val EXERCISE_TEST_DB = "exercise-migration-test.db"
    }
}
