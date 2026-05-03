package com.burak.healthapp.data.nutrition

import android.content.Context
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
class AssetNutritionPresetDataSource(
    private val context: Context,
) {
    private val mutex = Mutex()
    private var cachedFoods: List<NutritionPresetFood>? = null

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun loadFoods(): List<NutritionPresetFood> = cachedFoods ?: mutex.withLock {
        cachedFoods ?: withContext(Dispatchers.IO) {
            context.assets
                .open(ASSET_PATH)
                .bufferedReader()
                .use { reader -> json.decodeFromString<NutritionPresetDatasetDto>(reader.readText()) }
                .foods
                .map { it.toDomain() }
                .also { foods -> cachedFoods = foods }
        }
    }

    private companion object {
        const val ASSET_PATH = "nutrition_presets/nutrition_presets_tr_v1.json"
    }
}
