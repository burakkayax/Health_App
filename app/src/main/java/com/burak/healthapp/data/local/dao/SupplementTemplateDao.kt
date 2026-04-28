package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
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
