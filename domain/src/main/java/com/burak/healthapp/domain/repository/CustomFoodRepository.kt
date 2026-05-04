package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.model.nutrition.CustomFood
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user-created custom food items stored locally.
 */
interface CustomFoodRepository {
    fun observeAll(): Flow<List<CustomFood>>
    suspend fun getAll(): List<CustomFood>
    suspend fun searchCustomFoods(query: String): List<CustomFood>
    suspend fun getById(id: Long): CustomFood?
    suspend fun save(food: CustomFood): Long
    suspend fun delete(id: Long)
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    suspend fun deleteAll()
}
