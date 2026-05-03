package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.burak.healthapp.data.local.entity.CustomFoodEntity

@Dao
interface CustomFoodDao {
    @Query("SELECT * FROM custom_foods ORDER BY isFavorite DESC, updatedAt DESC")
    suspend fun getAll(): List<CustomFoodEntity>

    @Query(
        """
        SELECT * FROM custom_foods
        WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'
        ORDER BY isFavorite DESC, updatedAt DESC
        """,
    )
    suspend fun search(query: String): List<CustomFoodEntity>

    @Query("SELECT * FROM custom_foods WHERE id = :id")
    suspend fun getById(id: Long): CustomFoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CustomFoodEntity): Long

    @Query("DELETE FROM custom_foods WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE custom_foods SET isFavorite = :isFavorite, updatedAt = :now WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, now: String)

    @Query("DELETE FROM custom_foods")
    suspend fun deleteAll()
}
