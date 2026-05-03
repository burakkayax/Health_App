package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood

interface NutritionPresetRepository {
    suspend fun getAllPresets(): List<NutritionPresetFood>
    suspend fun getCategories(): List<String>
    suspend fun getPresetById(id: String): NutritionPresetFood?
    suspend fun searchPresets(
        query: String,
        category: String? = null,
        limit: Int = 50,
    ): List<NutritionPresetFood>
}
