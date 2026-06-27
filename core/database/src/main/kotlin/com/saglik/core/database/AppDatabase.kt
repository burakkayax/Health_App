package com.saglik.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saglik.core.database.converter.DateTimeConverters
import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.StepsDao
import com.saglik.core.database.dao.UserProfileDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.database.entity.AppMetadataEntity
import com.saglik.core.database.entity.ExerciseSessionEntity
import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.database.entity.StepsEntryEntity
import com.saglik.core.database.entity.UserProfileEntity
import com.saglik.core.database.entity.WeightEntryEntity

@Database(
    entities = [
        AppMetadataEntity::class,
        UserProfileEntity::class,
        WeightEntryEntity::class,
        SleepEntryEntity::class,
        StepsEntryEntity::class,
        ExerciseSessionEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    abstract fun weightDao(): WeightDao

    abstract fun sleepDao(): SleepDao

    abstract fun stepsDao(): StepsDao

    abstract fun exerciseDao(): ExerciseDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_profile (
                        id TEXT NOT NULL PRIMARY KEY,
                        sex TEXT NOT NULL,
                        age INTEGER,
                        birthDate TEXT,
                        heightCm REAL NOT NULL,
                        goal TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weight_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        weightKg REAL NOT NULL,
                        recordedAt INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        note TEXT
                    )
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sleep_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        quality TEXT,
                        source TEXT NOT NULL,
                        note TEXT
                    )
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN sourceRecordId TEXT")
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN sourcePackageName TEXT")
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN sourceAppName TEXT")
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN lastSyncedAt INTEGER")
                db.execSQL("ALTER TABLE weight_entries ADD COLUMN deletedAt INTEGER")
                db.execSQL(
                    """
                    UPDATE weight_entries
                    SET createdAt = recordedAt,
                        updatedAt = recordedAt
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_weight_entries_external_identity
                    ON weight_entries(source, sourcePackageName, sourceRecordId)
                    """.trimIndent(),
                )

                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN sourceRecordId TEXT")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN sourcePackageName TEXT")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN sourceAppName TEXT")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN lastSyncedAt INTEGER")
                db.execSQL("ALTER TABLE sleep_entries ADD COLUMN deletedAt INTEGER")
                db.execSQL(
                    """
                    UPDATE sleep_entries
                    SET createdAt = endTime,
                        updatedAt = endTime
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_sleep_entries_external_identity
                    ON sleep_entries(source, sourcePackageName, sourceRecordId)
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS steps_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        count INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        note TEXT,
                        sourceRecordId TEXT,
                        sourcePackageName TEXT,
                        sourceAppName TEXT,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        lastSyncedAt INTEGER,
                        deletedAt INTEGER
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_steps_entries_external_identity
                    ON steps_entries(source, sourcePackageName, sourceRecordId)
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exercise_sessions (
                        id TEXT NOT NULL PRIMARY KEY,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        exerciseType TEXT NOT NULL,
                        title TEXT,
                        notes TEXT,
                        source TEXT NOT NULL,
                        sourceRecordId TEXT,
                        sourcePackageName TEXT,
                        sourceAppName TEXT,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        lastSyncedAt INTEGER,
                        deletedAt INTEGER
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_sessions_external_identity
                    ON exercise_sessions(source, sourcePackageName, sourceRecordId)
                    """.trimIndent(),
                )
            }
        }
    }
}
