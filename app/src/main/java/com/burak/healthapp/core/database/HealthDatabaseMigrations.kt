package com.burak.healthapp.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
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

val MIGRATION_2_3 = object : Migration(2, 3) {
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

val MIGRATION_3_4 = object : Migration(3, 4) {
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
