package com.burak.healthapp.data.nutrition

import com.burak.healthapp.data.local.dao.CustomFoodDao
import com.burak.healthapp.data.local.entity.CustomFoodEntity
import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.repository.CustomFoodRepository
import java.time.LocalDateTime

class CustomFoodRepositoryImpl(
    private val dao: CustomFoodDao,
) : CustomFoodRepository {
    override suspend fun getAll(): List<CustomFood> =
        dao.getAll().map { it.toDomain() }

    override suspend fun searchCustomFoods(query: String): List<CustomFood> =
        if (query.isBlank()) dao.getAll().map { it.toDomain() }
        else dao.search(query.trim()).map { it.toDomain() }

    override suspend fun getById(id: Long): CustomFood? =
        dao.getById(id)?.toDomain()

    override suspend fun save(food: CustomFood): Long {
        val now = LocalDateTime.now()
        val entity = food.toEntity().copy(updatedAt = now)
        return dao.upsert(
            if (entity.id == 0L) entity.copy(createdAt = now) else entity,
        )
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite, LocalDateTime.now().toString())
    }

    override suspend fun deleteAll() = dao.deleteAll()
}

internal fun CustomFoodEntity.toDomain(): CustomFood = CustomFood(
    id = id,
    name = name,
    brand = brand,
    servingName = servingName,
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sugarGrams = sugarGrams,
    sodiumMg = sodiumMg,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun CustomFood.toEntity(): CustomFoodEntity = CustomFoodEntity(
    id = id,
    name = name,
    brand = brand,
    servingName = servingName,
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sugarGrams = sugarGrams,
    sodiumMg = sodiumMg,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
