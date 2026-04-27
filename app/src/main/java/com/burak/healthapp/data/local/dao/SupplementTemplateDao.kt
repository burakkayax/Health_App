package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
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
interface SupplementTemplateDao {
    @Query("SELECT * FROM supplement_templates WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun observeActive(): Flow<List<SupplementTemplateEntity>>

    @Query("SELECT * FROM supplement_templates ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<SupplementTemplateEntity>

    @Insert
    suspend fun insert(template: SupplementTemplateEntity): Long

    @Upsert
    suspend fun upsertAll(templates: List<SupplementTemplateEntity>)

    @Query("UPDATE supplement_templates SET isActive = 0 WHERE id IN (:ids)")
    suspend fun deactivate(ids: List<Long>)

    @Query("DELETE FROM supplement_templates")
    suspend fun deleteAll()
}
