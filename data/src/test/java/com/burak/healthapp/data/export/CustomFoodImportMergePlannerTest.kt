package com.burak.healthapp.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class CustomFoodImportMergePlannerTest {
    @Test
    fun customFoodIdentity_ignoresMutableMacroValues() {
        val existing = customFood(
            name = "Sütlü kahve",
            brand = "Cafe",
            servingName = "fincan",
            servingGrams = 200f,
            calories = 100,
            proteinGrams = 4,
            carbsGrams = 10,
            fatGrams = 4,
        )
        val imported = customFood(
            name = "Sütlü kahve",
            brand = "Cafe",
            servingName = "fincan",
            servingGrams = 200f,
            calories = 140,
            proteinGrams = 5,
            carbsGrams = 14,
            fatGrams = 6,
        )

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        assertTrue(plan.inserts.isEmpty())
        assertTrue(plan.updates.isEmpty())
        assertEquals(1, plan.recordsAfterImport.size)
    }

    @Test
    fun customFoodIdentity_includesServingName() {
        val cup = customFood(name = "Yulaf", servingName = "kase")
        val portion = customFood(name = "Yulaf", servingName = "porsiyon")

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(cup),
            imported = listOf(portion),
        )

        assertEquals(1, plan.inserts.size)
    }

    @Test
    fun customFoodIdentity_includesServingGrams() {
        val hundredGrams = customFood(name = "Yulaf", servingGrams = 100f)
        val fiftyGrams = customFood(name = "Yulaf", servingGrams = 50f)

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(hundredGrams),
            imported = listOf(fiftyGrams),
        )

        assertEquals(1, plan.inserts.size)
    }

    @Test
    fun customFoodIdentity_normalizesTurkishCharacters() {
        val existing = customFood(name = "Sütlü kahve", brand = "Café")
        val imported = customFood(name = "Sutlu kahve", brand = "Cafe")

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        assertTrue(plan.inserts.isEmpty())
        assertTrue(plan.updates.isEmpty())
    }

    @Test
    fun importPlanner_sameExportTwice_doesNotDuplicateCustomFoods() {
        val food = customFood(name = "Yulaf")
        val firstImport = CustomFoodImportMergePlanner.plan(
            existing = emptyList(),
            imported = listOf(food),
        )
        val secondImport = CustomFoodImportMergePlanner.plan(
            existing = firstImport.recordsAfterImport,
            imported = listOf(food),
        )

        assertEquals(1, firstImport.inserts.size)
        assertTrue(secondImport.inserts.isEmpty())
        assertTrue(secondImport.updates.isEmpty())
        assertEquals(1, secondImport.recordsAfterImport.size)
    }

    @Test
    fun importPlanner_newerUpdatedAt_updatesExistingCustomFood() {
        val existing = customFood(
            name = "Yulaf",
            calories = 300,
            updatedAt = TIME_10,
        )
        val imported = customFood(
            name = "Yulaf",
            calories = 350,
            updatedAt = TIME_11,
        )

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        assertEquals(1, plan.updates.size)
        assertEquals(350, plan.updates.single().imported.calories)
    }

    @Test
    fun importPlanner_olderUpdatedAt_keepsExistingCustomFood() {
        val existing = customFood(
            name = "Yulaf",
            calories = 350,
            updatedAt = TIME_11,
        )
        val imported = customFood(
            name = "Yulaf",
            calories = 300,
            updatedAt = TIME_10,
        )

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        assertTrue(plan.inserts.isEmpty())
        assertTrue(plan.updates.isEmpty())
        assertEquals(350, plan.recordsAfterImport.single().calories)
    }

    @Test
    fun importPlanner_differentServingName_keepsSeparateRecords() {
        val existing = customFood(name = "Yulaf", servingName = "kase")
        val imported = customFood(name = "Yulaf", servingName = "porsiyon")

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        assertEquals(1, plan.inserts.size)
        assertEquals(2, plan.recordsAfterImport.size)
    }

    @Test
    fun importPlanner_differentServingGrams_keepsSeparateRecords() {
        val existing = customFood(name = "Yulaf", servingGrams = 100f)
        val imported = customFood(name = "Yulaf", servingGrams = 50f)

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        assertEquals(1, plan.inserts.size)
        assertEquals(2, plan.recordsAfterImport.size)
    }

    @Test
    fun importPlanner_optionalNutrients_preservedWhenOlderImportIgnored() {
        val existing = customFood(
            name = "Yulaf",
            fiberGrams = 5.5f,
            sugarGrams = 2f,
            sodiumMg = 120f,
            updatedAt = TIME_11,
        )
        val imported = customFood(
            name = "Yulaf",
            fiberGrams = null,
            sugarGrams = null,
            sodiumMg = null,
            updatedAt = TIME_10,
        )

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        val kept = plan.recordsAfterImport.single()
        assertEquals(5.5f, kept.fiberGrams)
        assertEquals(2f, kept.sugarGrams)
        assertEquals(120f, kept.sodiumMg)
    }

    @Test
    fun importPlanner_optionalNutrients_updatedWhenNewerImportAccepted() {
        val existing = customFood(
            name = "Yulaf",
            fiberGrams = 5.5f,
            sugarGrams = 2f,
            sodiumMg = 120f,
            updatedAt = TIME_10,
        )
        val imported = customFood(
            name = "Yulaf",
            fiberGrams = 7f,
            sugarGrams = 3f,
            sodiumMg = 80f,
            updatedAt = TIME_11,
        )

        val plan = CustomFoodImportMergePlanner.plan(
            existing = listOf(existing),
            imported = listOf(imported),
        )

        val update = plan.updates.single().imported
        assertEquals(7f, update.fiberGrams)
        assertEquals(3f, update.sugarGrams)
        assertEquals(80f, update.sodiumMg)
    }

    private fun customFood(
        id: Long = 1L,
        name: String,
        brand: String? = null,
        servingName: String = "porsiyon",
        servingGrams: Float = 100f,
        calories: Int = 300,
        proteinGrams: Int = 10,
        carbsGrams: Int = 50,
        fatGrams: Int = 5,
        fiberGrams: Float? = null,
        sugarGrams: Float? = null,
        sodiumMg: Float? = null,
        updatedAt: LocalDateTime = TIME_10,
    ): CustomFoodImportRecord = CustomFoodImportRecord(
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
        isFavorite = false,
        createdAt = TIME_10,
        updatedAt = updatedAt,
    )

    private companion object {
        val TIME_10: LocalDateTime = LocalDateTime.parse("2026-04-27T10:00:00")
        val TIME_11: LocalDateTime = LocalDateTime.parse("2026-04-27T11:00:00")
    }
}
