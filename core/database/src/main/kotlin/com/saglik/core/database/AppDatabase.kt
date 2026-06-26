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
    version = 3,
    exportSchema = false,
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
    }
}
