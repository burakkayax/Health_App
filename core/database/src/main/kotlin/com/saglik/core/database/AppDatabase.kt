package com.saglik.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saglik.core.database.converter.DateTimeConverters
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.UserProfileDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.database.entity.AppMetadataEntity
import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.database.entity.UserProfileEntity
import com.saglik.core.database.entity.WeightEntryEntity

@Database(
    entities = [
        AppMetadataEntity::class,
        UserProfileEntity::class,
        WeightEntryEntity::class,
        SleepEntryEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    abstract fun weightDao(): WeightDao

    abstract fun sleepDao(): SleepDao

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
    }
}
