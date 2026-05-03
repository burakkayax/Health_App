package com.burak.healthapp.domain.model.nutrition

import java.time.LocalDateTime

/**
 * A custom food item stored locally on the device.
 * Unlike [NutritionPresetFood], this is user-created and fully editable.
 */
data class CustomFood(
    val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val servingName: String,
    val servingGrams: Float,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val fiberGrams: Float? = null,
    val sugarGrams: Float? = null,
    val sodiumMg: Float? = null,
    val isFavorite: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
