package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
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
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementCheckDao {
    @Query("SELECT * FROM supplement_checks WHERE date = :date")
    fun observeForDate(date: LocalDate): Flow<List<SupplementCheckEntity>>

    @Upsert
    suspend fun upsert(check: SupplementCheckEntity)

    @Query("DELETE FROM supplement_checks")
    suspend fun deleteAll()
}
