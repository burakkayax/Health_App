package com.burak.healthapp.data.nutrition

import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import com.burak.healthapp.domain.repository.NutritionPresetRepository

class NutritionPresetRepositoryImpl(
    private val dataSource: AssetNutritionPresetDataSource,
) : NutritionPresetRepository {
    override suspend fun getAllPresets(): List<NutritionPresetFood> = dataSource.loadFoods()

    override suspend fun getCategories(): List<String> = getAllPresets()
        .map { it.categoryTr }
        .distinct()
        .sorted()

    override suspend fun getPresetById(id: String): NutritionPresetFood? = getAllPresets()
        .firstOrNull { it.id == id }

    override suspend fun searchPresets(
        query: String,
        category: String?,
        limit: Int,
    ): List<NutritionPresetFood> {
        val normalizedQuery = TurkishSearchNormalizer.normalize(query)
        return getAllPresets()
            .asSequence()
            .filter { food -> category == null || food.categoryTr == category }
            .map { food -> food to food.scoreFor(normalizedQuery) }
            .filter { (_, score) -> normalizedQuery.isBlank() || score > 0 }
            .sortedWith(
                compareByDescending<Pair<NutritionPresetFood, Int>> { it.second }
                    .thenBy { it.first.nameTr },
            )
            .take(limit.coerceAtLeast(1))
            .map { it.first }
            .toList()
    }

    private fun NutritionPresetFood.scoreFor(normalizedQuery: String): Int {
        if (normalizedQuery.isBlank()) return 1
        val fields = buildList {
            add(nameTr)
            add(categoryTr)
            addAll(aliasesTr)
            addAll(searchTermsTr)
            nameEnSource?.let(::add)
        }.map { TurkishSearchNormalizer.normalize(it) }
        return when {
            fields.any { it == normalizedQuery } -> 100
            fields.any { it.startsWith(normalizedQuery) } -> 80
            fields.any { it.contains(normalizedQuery) } -> 50
            normalizedQuery.split(' ').all { token -> fields.any { it.contains(token) } } -> 30
            else -> 0
        }
    }
}

