package com.burak.healthapp.core.di

import android.content.Context
import androidx.room.Room
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.core.database.MIGRATION_1_2
import com.burak.healthapp.core.database.MIGRATION_2_3
import com.burak.healthapp.core.database.MIGRATION_3_4
import com.burak.healthapp.core.database.MIGRATION_4_5
import com.burak.healthapp.core.database.MIGRATION_5_6
import com.burak.healthapp.core.database.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideHealthDatabase(
        @ApplicationContext context: Context,
    ): HealthDatabase = Room.databaseBuilder(
        context,
        HealthDatabase::class.java,
        "health.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
        .build()

    @Provides
    fun provideMealDao(database: HealthDatabase) = database.mealDao()

    @Provides
    fun provideHydrationDao(database: HealthDatabase) = database.hydrationDao()

    @Provides
    fun provideSleepDao(database: HealthDatabase) = database.sleepDao()

    @Provides
    fun provideExerciseDao(database: HealthDatabase) = database.exerciseDao()

    @Provides
    fun provideSmokingDao(database: HealthDatabase) = database.smokingDao()

    @Provides
    fun provideStepDao(database: HealthDatabase) = database.stepDao()

    @Provides
    fun provideCaffeineDao(database: HealthDatabase) = database.caffeineDao()

    @Provides
    fun provideSupplementTemplateDao(database: HealthDatabase) = database.supplementTemplateDao()

    @Provides
    fun provideSupplementDoseDao(database: HealthDatabase) = database.supplementDoseDao()

    @Provides
    fun provideBodyMeasurementDao(database: HealthDatabase) = database.bodyMeasurementDao()

    @Provides
    fun provideCustomFoodDao(database: HealthDatabase) = database.customFoodDao()
}
