package com.burak.healthapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.ExerciseDao
import com.burak.healthapp.data.local.dao.HydrationDao
import com.burak.healthapp.data.local.dao.MealDao
import com.burak.healthapp.data.local.dao.SleepDao
import com.burak.healthapp.data.local.dao.SmokingDao
import com.burak.healthapp.data.local.dao.StepDao
import com.burak.healthapp.data.local.dao.SupplementCheckDao
import com.burak.healthapp.data.local.dao.SupplementDoseDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementCheckEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity

@Database(
    entities = [
        MealEntryEntity::class,
        HydrationEntryEntity::class,
        SleepSessionEntity::class,
        ExerciseEntryEntity::class,
        SmokingEntryEntity::class,
        SupplementTemplateEntity::class,
        SupplementCheckEntity::class,
        SupplementDoseEntryEntity::class,
        BodyMeasurementEntity::class,
        StepEntryEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(DateTimeConverters::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun sleepDao(): SleepDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun smokingDao(): SmokingDao
    abstract fun stepDao(): StepDao
    abstract fun supplementTemplateDao(): SupplementTemplateDao
    abstract fun supplementCheckDao(): SupplementCheckDao
    abstract fun supplementDoseDao(): SupplementDoseDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
}
