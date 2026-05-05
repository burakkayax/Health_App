package com.burak.healthapp.data.export

import com.burak.healthapp.data.nutrition.TurkishSearchNormalizer
import java.time.LocalDateTime
import java.util.Locale

internal data class CustomFoodImportRecord(
    val id: Long,
    val name: String,
    val brand: String?,
    val servingName: String,
    val servingGrams: Float,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val fiberGrams: Float?,
    val sugarGrams: Float?,
    val sodiumMg: Float?,
    val isFavorite: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

internal data class CustomFoodImportUpdate(
    val existing: CustomFoodImportRecord,
    val imported: CustomFoodImportRecord,
)

internal data class CustomFoodImportPlan(
    val inserts: List<CustomFoodImportRecord>,
    val updates: List<CustomFoodImportUpdate>,
    val recordsAfterImport: List<CustomFoodImportRecord>,
)

internal object CustomFoodImportMergePlanner {
    fun contentKey(
        name: String,
        brand: String?,
        servingName: String,
        servingGrams: Float,
    ): String {
        val normalizedName = TurkishSearchNormalizer.normalize(name)
        val normalizedBrand = TurkishSearchNormalizer.normalize(brand.orEmpty())
        val normalizedServingName = TurkishSearchNormalizer.normalize(servingName)
        val servingKey = "%.2f".format(Locale.US, servingGrams)
        return listOf(
            normalizedName,
            normalizedBrand,
            normalizedServingName,
            servingKey,
        ).joinToString("|")
    }

    fun plan(
        existing: List<CustomFoodImportRecord>,
        imported: List<CustomFoodImportRecord>,
    ): CustomFoodImportPlan {
        val existingByKey = existing.associateBy { it.contentKey() }.toMutableMap()
        val importedByKey = newestByContentKey(imported)
        val inserts = mutableListOf<CustomFoodImportRecord>()
        val updates = mutableListOf<CustomFoodImportUpdate>()

        importedByKey.forEach { (key, importedRecord) ->
            val existingRecord = existingByKey[key]
            when {
                existingRecord == null -> {
                    inserts += importedRecord
                    existingByKey[key] = importedRecord
                }
                importedRecord.updatedAt.isAfter(existingRecord.updatedAt) -> {
                    updates += CustomFoodImportUpdate(
                        existing = existingRecord,
                        imported = importedRecord,
                    )
                    existingByKey[key] = importedRecord.copy(
                        id = existingRecord.id,
                        createdAt = existingRecord.createdAt,
                    )
                }
            }
        }

        return CustomFoodImportPlan(
            inserts = inserts,
            updates = updates,
            recordsAfterImport = existingByKey.values.toList(),
        )
    }

    private fun newestByContentKey(
        records: List<CustomFoodImportRecord>,
    ): Map<String, CustomFoodImportRecord> {
        val recordsByKey = linkedMapOf<String, CustomFoodImportRecord>()
        records.forEach { record ->
            val key = record.contentKey()
            val existing = recordsByKey[key]
            if (existing == null || record.updatedAt.isAfter(existing.updatedAt)) {
                recordsByKey[key] = record
            }
        }
        return recordsByKey
    }
}

internal fun CustomFoodImportRecord.contentKey(): String = CustomFoodImportMergePlanner.contentKey(
    name = name,
    brand = brand,
    servingName = servingName,
    servingGrams = servingGrams,
)
