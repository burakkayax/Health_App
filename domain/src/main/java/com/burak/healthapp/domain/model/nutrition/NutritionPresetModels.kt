package com.burak.healthapp.domain.model.nutrition

data class NutritionPresetFood(
    val id: String,
    val slug: String,
    val nameTr: String,
    val nameTrOriginal: String?,
    val aliasesTr: List<String>,
    val searchTermsTr: List<String>,
    val nameEnSource: String?,
    val categoryTr: String,
    val defaultServing: NutritionServing,
    val commonServings: List<NutritionServing>,
    val basis: String,
    val source: NutritionDataSource,
    val nutrientsPer100g: NutrientProfile,
    val nutrientsPerDefaultServing: NutrientProfile,
    val dataQuality: NutritionDataQuality,
) {
    fun nutrientsForGrams(grams: Float): NutrientProfile = nutrientsPer100g.scaledBy(grams / 100f)
}

data class NutritionServing(
    val nameTr: String,
    val grams: Float,
)

data class NutritionDataSource(
    val dataset: String,
    val sourceId: String,
    val sourceCategory: String?,
    val accessDate: String?,
    val licenseNote: String?,
)

data class NutritionDataQuality(
    val level: NutritionDataQualityLevel,
    val notesTr: String,
)

enum class NutritionDataQualityLevel {
    HIGH,
    MEDIUM,
    LOW,
}

data class NutrientProfile(
    val energyKcal: Float,
    val proteinG: Float,
    val fatG: Float,
    val carbsG: Float,
    val fiberG: Float? = null,
    val sugarsG: Float? = null,
    val saturatedFatG: Float? = null,
    val transFatG: Float? = null,
    val cholesterolMg: Float? = null,
    val sodiumMg: Float? = null,
    val potassiumMg: Float? = null,
    val calciumMg: Float? = null,
    val ironMg: Float? = null,
    val magnesiumMg: Float? = null,
    val phosphorusMg: Float? = null,
    val zincMg: Float? = null,
    val copperMg: Float? = null,
    val manganeseMg: Float? = null,
    val seleniumUg: Float? = null,
    val vitaminAUgRae: Float? = null,
    val vitaminCMg: Float? = null,
    val vitaminDUg: Float? = null,
    val vitaminEMg: Float? = null,
    val vitaminKUg: Float? = null,
    val thiaminMg: Float? = null,
    val riboflavinMg: Float? = null,
    val niacinMg: Float? = null,
    val vitaminB6Mg: Float? = null,
    val folateUg: Float? = null,
    val vitaminB12Ug: Float? = null,
) {
    fun scaledBy(multiplier: Float): NutrientProfile = copy(
        energyKcal = energyKcal * multiplier,
        proteinG = proteinG * multiplier,
        fatG = fatG * multiplier,
        carbsG = carbsG * multiplier,
        fiberG = fiberG?.times(multiplier),
        sugarsG = sugarsG?.times(multiplier),
        saturatedFatG = saturatedFatG?.times(multiplier),
        transFatG = transFatG?.times(multiplier),
        cholesterolMg = cholesterolMg?.times(multiplier),
        sodiumMg = sodiumMg?.times(multiplier),
        potassiumMg = potassiumMg?.times(multiplier),
        calciumMg = calciumMg?.times(multiplier),
        ironMg = ironMg?.times(multiplier),
        magnesiumMg = magnesiumMg?.times(multiplier),
        phosphorusMg = phosphorusMg?.times(multiplier),
        zincMg = zincMg?.times(multiplier),
        copperMg = copperMg?.times(multiplier),
        manganeseMg = manganeseMg?.times(multiplier),
        seleniumUg = seleniumUg?.times(multiplier),
        vitaminAUgRae = vitaminAUgRae?.times(multiplier),
        vitaminCMg = vitaminCMg?.times(multiplier),
        vitaminDUg = vitaminDUg?.times(multiplier),
        vitaminEMg = vitaminEMg?.times(multiplier),
        vitaminKUg = vitaminKUg?.times(multiplier),
        thiaminMg = thiaminMg?.times(multiplier),
        riboflavinMg = riboflavinMg?.times(multiplier),
        niacinMg = niacinMg?.times(multiplier),
        vitaminB6Mg = vitaminB6Mg?.times(multiplier),
        folateUg = folateUg?.times(multiplier),
        vitaminB12Ug = vitaminB12Ug?.times(multiplier),
    )
}
