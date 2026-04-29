package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType

fun MealEntryEntity.toDomain(): MealEntry = MealEntry(
    id = id,
    date = date,
    mealType = MealType.valueOf(mealType),
    name = name,
    calories = calories,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    proteinGrams = proteinGrams,
    createdAt = createdAt,
)

fun MealEntry.toEntity(): MealEntryEntity = MealEntryEntity(
    id = id,
    date = date,
    mealType = mealType.name,
    name = name,
    calories = calories,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    proteinGrams = proteinGrams,
    createdAt = createdAt,
)
