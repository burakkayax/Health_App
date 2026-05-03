package com.burak.healthapp.data.nutrition

import com.burak.healthapp.domain.model.nutrition.NutrientProfile
import com.burak.healthapp.domain.model.nutrition.NutritionDataQuality
import com.burak.healthapp.domain.model.nutrition.NutritionDataQualityLevel
import com.burak.healthapp.domain.model.nutrition.NutritionDataSource
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import com.burak.healthapp.domain.model.nutrition.NutritionServing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NutritionPresetDatasetDto(
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("dataset_name") val datasetName: String,
    val locale: String,
    val foods: List<NutritionPresetFoodDto>,
)

@Serializable
internal data class NutritionPresetFoodDto(
    val id: String,
    val slug: String,
    @SerialName("name_tr") val nameTr: String,
    @SerialName("name_tr_original") val nameTrOriginal: String? = null,
    @SerialName("aliases_tr") val aliasesTr: List<String> = emptyList(),
    @SerialName("search_terms_tr") val searchTermsTr: List<String> = emptyList(),
    @SerialName("name_en_source") val nameEnSource: String? = null,
    @SerialName("category_tr") val categoryTr: String,
    @SerialName("default_serving") val defaultServing: NutritionServingDto,
    @SerialName("common_servings") val commonServings: List<NutritionServingDto> = emptyList(),
    val basis: String,
    val source: NutritionDataSourceDto,
    @SerialName("nutrients_per_100g") val nutrientsPer100g: NutrientProfileDto,
    @SerialName("nutrients_per_default_serving") val nutrientsPerDefaultServing: NutrientProfileDto,
    @SerialName("data_quality") val dataQuality: NutritionDataQualityDto,
) {
    fun toDomain(): NutritionPresetFood = NutritionPresetFood(
        id = id,
        slug = slug,
        nameTr = nameTr,
        nameTrOriginal = nameTrOriginal,
        aliasesTr = aliasesTr,
        searchTermsTr = searchTermsTr,
        nameEnSource = nameEnSource,
        categoryTr = categoryTr,
        defaultServing = defaultServing.toDomain(),
        commonServings = commonServings.map { it.toDomain() },
        basis = basis,
        source = source.toDomain(),
        nutrientsPer100g = nutrientsPer100g.toDomain(),
        nutrientsPerDefaultServing = nutrientsPerDefaultServing.toDomain(),
        dataQuality = dataQuality.toDomain(),
    )
}

@Serializable
internal data class NutritionServingDto(
    @SerialName("name_tr") val nameTr: String,
    val grams: Float,
) {
    fun toDomain(): NutritionServing = NutritionServing(nameTr = nameTr, grams = grams)
}

@Serializable
internal data class NutritionDataSourceDto(
    val dataset: String,
    @SerialName("source_id") val sourceId: String,
    @SerialName("source_category") val sourceCategory: String? = null,
    @SerialName("access_date") val accessDate: String? = null,
    @SerialName("license_note") val licenseNote: String? = null,
) {
    fun toDomain(): NutritionDataSource = NutritionDataSource(
        dataset = dataset,
        sourceId = sourceId,
        sourceCategory = sourceCategory,
        accessDate = accessDate,
        licenseNote = licenseNote,
    )
}

@Serializable
internal data class NutritionDataQualityDto(
    val level: String,
    @SerialName("notes_tr") val notesTr: String,
) {
    fun toDomain(): NutritionDataQuality = NutritionDataQuality(
        level = when (level.lowercase()) {
            "high" -> NutritionDataQualityLevel.HIGH
            "low" -> NutritionDataQualityLevel.LOW
            else -> NutritionDataQualityLevel.MEDIUM
        },
        notesTr = notesTr,
    )
}

@Serializable
internal data class NutrientProfileDto(
    @SerialName("energy_kcal") val energyKcal: Float,
    @SerialName("protein_g") val proteinG: Float,
    @SerialName("fat_g") val fatG: Float,
    @SerialName("carbs_g") val carbsG: Float,
    @SerialName("fiber_g") val fiberG: Float? = null,
    @SerialName("sugars_g") val sugarsG: Float? = null,
    @SerialName("saturated_fat_g") val saturatedFatG: Float? = null,
    @SerialName("trans_fat_g") val transFatG: Float? = null,
    @SerialName("cholesterol_mg") val cholesterolMg: Float? = null,
    @SerialName("sodium_mg") val sodiumMg: Float? = null,
    @SerialName("potassium_mg") val potassiumMg: Float? = null,
    @SerialName("calcium_mg") val calciumMg: Float? = null,
    @SerialName("iron_mg") val ironMg: Float? = null,
    @SerialName("magnesium_mg") val magnesiumMg: Float? = null,
    @SerialName("phosphorus_mg") val phosphorusMg: Float? = null,
    @SerialName("zinc_mg") val zincMg: Float? = null,
    @SerialName("copper_mg") val copperMg: Float? = null,
    @SerialName("manganese_mg") val manganeseMg: Float? = null,
    @SerialName("selenium_ug") val seleniumUg: Float? = null,
    @SerialName("vitamin_a_ug_rae") val vitaminAUgRae: Float? = null,
    @SerialName("vitamin_c_mg") val vitaminCMg: Float? = null,
    @SerialName("vitamin_d_ug") val vitaminDUg: Float? = null,
    @SerialName("vitamin_e_mg") val vitaminEMg: Float? = null,
    @SerialName("vitamin_k_ug") val vitaminKUg: Float? = null,
    @SerialName("thiamin_mg") val thiaminMg: Float? = null,
    @SerialName("riboflavin_mg") val riboflavinMg: Float? = null,
    @SerialName("niacin_mg") val niacinMg: Float? = null,
    @SerialName("vitamin_b6_mg") val vitaminB6Mg: Float? = null,
    @SerialName("folate_ug") val folateUg: Float? = null,
    @SerialName("vitamin_b12_ug") val vitaminB12Ug: Float? = null,
) {
    fun toDomain(): NutrientProfile = NutrientProfile(
        energyKcal = energyKcal,
        proteinG = proteinG,
        fatG = fatG,
        carbsG = carbsG,
        fiberG = fiberG,
        sugarsG = sugarsG,
        saturatedFatG = saturatedFatG,
        transFatG = transFatG,
        cholesterolMg = cholesterolMg,
        sodiumMg = sodiumMg,
        potassiumMg = potassiumMg,
        calciumMg = calciumMg,
        ironMg = ironMg,
        magnesiumMg = magnesiumMg,
        phosphorusMg = phosphorusMg,
        zincMg = zincMg,
        copperMg = copperMg,
        manganeseMg = manganeseMg,
        seleniumUg = seleniumUg,
        vitaminAUgRae = vitaminAUgRae,
        vitaminCMg = vitaminCMg,
        vitaminDUg = vitaminDUg,
        vitaminEMg = vitaminEMg,
        vitaminKUg = vitaminKUg,
        thiaminMg = thiaminMg,
        riboflavinMg = riboflavinMg,
        niacinMg = niacinMg,
        vitaminB6Mg = vitaminB6Mg,
        folateUg = folateUg,
        vitaminB12Ug = vitaminB12Ug,
    )
}
